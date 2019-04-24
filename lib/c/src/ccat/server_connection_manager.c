/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "server_connection_manager.h"

#include "ccat/client_config.h"
#include "ccat/message_manager.h"
#include "ccat/router_json_parser.h"

#include "lib/headers.h"
#include "lib/cat_ae.h"
#include "lib/cat_anet.h"
#include "lib/cat_clog.h"
#include "lib/cat_mutex.h"

static sds g_server_responseBody = NULL;
static sds g_server_requestBuf = NULL;
static sds g_server_ips[64] = {0};
static int g_server_ports[64] = {0};

static volatile int g_server_count = 0;

volatile int g_server_activeId = -1;

static CATCRITICALSECTION g_server_lock = NULL;

extern CatMessageManager g_cat_messageManager;

extern char g_cat_send_ip[64];
extern int g_cat_send_port;
extern int g_cat_send_fd;
extern int g_cat_send_failedFlag;

static int tryConnBestServer() {
    int oldFd, newFd;
    if (g_server_activeId == 0) {
        return 1;
    }
    int ipValidNum = g_server_activeId;
    if (ipValidNum < 0) {
        ipValidNum = g_server_count;
    }

    int i = 0;
    for (; i < ipValidNum; ++i) {
        INNER_LOG(CLOG_INFO, "Try connect to server %s:%d.", g_server_ips[i], (int) g_server_ports[i]);
        newFd = catAnetTcpNonBlockConnect(NULL, g_server_ips[i], g_server_ports[i]);
        if (newFd > 0) {
            int retVal = 0;
#ifdef WIN32
            retVal = aeWait(newFd, AE_WRITABLE, 200);
#else
            retVal = catAeWait(newFd, AE_WRITABLE | AE_ERROR | AE_HUP, 200);
#endif
            if (retVal > 0 && !(retVal & AE_ERROR) && (retVal & AE_WRITABLE)) {
                INNER_LOG(CLOG_INFO, "Connect success.");
                g_server_activeId = i;
                strcpy(g_cat_send_ip, g_server_ips[i]);
                g_cat_send_port = g_server_ports[i];
                oldFd = g_cat_send_fd;
                g_cat_send_fd = newFd;

                struct linger linger;
                linger.l_onoff = 1;
                linger.l_linger = 0;
                setsockopt(g_cat_send_fd, SOL_SOCKET, SO_LINGER, &linger, sizeof(linger));

                if (oldFd > 0) {
                    catAnetClose(oldFd);
                }
                return 1;
            } else {
                INNER_LOG(CLOG_WARNING, "Cannot connect to server %s:%d.",
                          g_server_ips[i], (int) g_server_ports[i]);
                catAnetClose(newFd);
            }
        }
    }
    if (g_server_activeId >= 0)
        return 1;
    return 0;
}

static void updateCatActiveConnIndex() {
    if (g_server_activeId < 0) {
        return;
    }
    int i = 0;
    for (i = 0; i < g_server_count; ++i) {
        if (strcmp(g_server_ips[i], g_cat_send_ip) == 0 && g_server_ports[i] == g_cat_send_port) {
            g_server_activeId = i;
            return;
        }
    }
    g_server_activeId = -1;
}

static int checkIpValid(sds ip, size_t ipLen) {
    if (ipLen < 7 || ipLen > 15) {
        return 0;
    }
    size_t i = 0;
    int splitCount = 0;
    for (; i < ipLen; ++i) {
        if (ip[i] == '.') {
            ++splitCount;
        } else if (ip[i] < '0' || ip[i] > '9') {
            return 0;
        }
    }
    if (splitCount != 3) {
        return 0;
    }
    return 1;
}

static int resolveIpPortStr(sds ipPortStr, sds *ip, int *port) {
    size_t i = 0;

    for (; i < catsdslen(ipPortStr); ++i) {
        if (ipPortStr[i] == ':') {
            if (*ip == NULL) {
                *ip = catsdsnewEmpty(16);
            }
            if (!checkIpValid(ipPortStr, i)) {
                return 0;
            }
            *ip = catsdscpylen(*ip, ipPortStr, i);
            long lPort = strtol(ipPortStr + i + 1, NULL, 10);
            if (lPort <= 0 || lPort > 65536) {
                *port = 2280;
            } else {
                *port = (int) lPort;
            }
            return 1;
        }
    }
    return 0;
}

int resolveServerIps(char *routerIps) {
    int i = 0;
    int count = 0;
    int validIpCount = 0;
    sds *spIpPorts = catsdssplitlen(routerIps, strlen(routerIps), ";", 1, &count);

    if (spIpPorts != NULL && count > 0) {
    } else {
        return 0;
    }


    for (i = 0; i < g_server_count; ++i) {
        catsdsfree(g_server_ips[i]);
        g_server_ips[i] = NULL;
    }

    for (i = 0; i < count && i < 64; ++i) {
        if (resolveIpPortStr(spIpPorts[i], g_server_ips + validIpCount, g_server_ports + validIpCount) > 0) {
            ++validIpCount;
        }
    }
    catsdsfreesplitres(spIpPorts, count);
    g_server_count = validIpCount;
    return validIpCount;
}

static sds inline _buildHttpHeader(
        sds buf,
        const char *hostname,
        int port,
        const char *uri
) {
    if (80 == port) {
        buf = catsdscatprintf(buf, "GET http://%s%s HTTP/1.0\r\n", hostname, uri);
    } else {
        buf = catsdscatprintf(buf, "GET http://%s:%d%s HTTP/1.0\r\n", hostname, port, uri);
    }
    buf = catsdscatprintf(buf, "Host: %s\r\n", hostname);
    buf = catsdscatprintf(buf, "Connection: close\r\n\r\n");
    return buf;
}

static int getRouterFromServer(char *hostName, unsigned int port, char *domain) {
    if (g_server_requestBuf == NULL) {
        g_server_requestBuf = catsdsnewEmpty(1024);
        catChecktPtr(g_server_requestBuf);
    }

    char destIP[128];
    if (catAnetResolveIP(NULL, hostName, destIP, 128) == ANET_ERR) {
        return 0;
    }
    INNER_LOG(CLOG_INFO, "Start connect to router server %s : %hd.", destIP, port);

    int sockfd = catAnetTcpNonBlockConnect(NULL, destIP, port);
    if (sockfd < 0) {
        INNER_LOG(CLOG_WARNING, "Connect to router server %s : %hd Error.", destIP, port);
        return 0;
    }

    // wait newFd to be writable
    int retVal = 0;
#ifdef WIN32
    retVal = aeWait(sockfd, AE_WRITABLE, 200);
#else
    retVal = catAeWait(sockfd, AE_WRITABLE | AE_ERROR | AE_HUP, 200);
#endif
    if (retVal > 0 && !(retVal & AE_ERROR) && (retVal & AE_WRITABLE)) {
        INNER_LOG(CLOG_INFO, "Connect to router server %s : %hd Success.", destIP, port);
    } else {
        INNER_LOG(CLOG_WARNING, "Connect to router server %s : %hd Error, timeout.", destIP, port);
        catAnetClose(sockfd);
        return 0;
    }

    char uri[512];
    snprintf(uri, 511, "/cat/s/router?op=json&domain=%s&ip=%s&hostname=%s", domain, g_cat_messageManager.ip,
             g_cat_messageManager.hostname);

    catsdsclear(g_server_requestBuf);
    g_server_requestBuf = _buildHttpHeader(g_server_requestBuf, hostName, port, uri);

    int status = catAnetBlockWriteTime(sockfd, g_server_requestBuf, (int) catsdslen(g_server_requestBuf), 100);
    if (status == ANET_ERR) {
        catAnetClose(sockfd);
        return 0;
    }
    char resp[2048];

    // wait 200 ms
    status = catAnetBlockReadTime(sockfd, resp, 2047, 1000);
    if (status == ANET_ERR || status < 4) {
        catAnetClose(sockfd);
        return 0;
    }
    resp[status] = '\0';
    char *t = strstr(resp, "\r\n\r\n");
    if (!t) {
        catAnetClose(sockfd);
        return 0;
    }
    char *body = t + 4;
    if (body[0] == '\0') {
        catAnetClose(sockfd);
        return 0;
    }
    catAnetClose(sockfd);
    INNER_LOG(CLOG_INFO, "Got available server list:\n%s", body);

    if (g_server_responseBody == NULL) {
        g_server_responseBody = catsdsnewEmpty(2048);
        catChecktPtr(g_server_responseBody);
    } else {
        if (strcmp(g_server_responseBody, body) == 0) {
            return g_server_count;
        }
    }
    g_server_responseBody = catsdscpy(g_server_responseBody, body);

    return parseCatJsonRouter(g_server_responseBody);
}

int recoverCatServerConn() {
    CATCS_ENTER(g_server_lock);
    catAnetClose(g_cat_send_fd);
    g_cat_send_fd = -1;
    g_server_activeId = -1;
    if (!tryConnBestServer()) {
        INNER_LOG(CLOG_WARNING, "Failed to reconnect server, trying updating routing table.");
        if (!updateCatServerConn()) {
            INNER_LOG(CLOG_ERROR, "Retry failed, server is unavailable.");
            CATCS_LEAVE(g_server_lock);
            return 0;
        }
    }
    g_cat_send_failedFlag = 1;
    CATCS_LEAVE(g_server_lock);
    return 1;
}

void initCatServerConnManager() {
    g_server_lock = CATCreateCriticalSection();

    // 先从配置读初始的服务器配置
    g_server_count = g_config.serverNum;
    if (g_server_count > 64) {
        g_server_count = 64;
    }
    int i = 0;
    int validCount = 0;
    for (; i < g_server_count; ++i) {
        if (resolveIpPortStr(g_config.serverAddresses[i], g_server_ips + validCount, g_server_ports + validCount)) {
            ++validCount;
        }
    }
    g_server_count = validCount;
}

void clearCatServerConnManager() {
    int i = 0;
    CATDeleteCriticalSection(g_server_lock);
    if (g_cat_send_fd > 0) {
        catAnetClose(g_cat_send_fd);
    }
    for (i = 0; i < g_server_count; ++i) {
        catsdsfree(g_server_ips[i]);
        g_server_ips[i] = NULL;
    }

    if (g_server_responseBody != NULL) {
        catsdsfree(g_server_responseBody);
        g_server_responseBody = NULL;
    }
    if (g_server_requestBuf != NULL) {
        catsdsfree(g_server_requestBuf);
        g_server_requestBuf = NULL;
    }
}

int socketConnected(int sock) {
#if defined(__APPLE__)
    return 1;
#else
    if (sock <= 0)
        return 0;

    struct tcp_info info;
    int len = sizeof(info);
    getsockopt(sock, IPPROTO_TCP, TCP_INFO, &info, (socklen_t *) &len);

    if (info.tcpi_state == 1) {
        return 1;
    } else {
        return 0;
    }
#endif
}

int checkCatActiveConn() {
    CATCS_ENTER(g_server_lock);

    if (!socketConnected(g_cat_send_fd)) {
        INNER_LOG(CLOG_WARNING, "Connection has been reset, reconnecting.");

        if (g_cat_send_fd > 0) {
            catAnetClose(g_cat_send_fd);
            g_cat_send_fd = -1;
            g_server_activeId = -1;
            g_cat_send_failedFlag = 1;
        }

        if (g_cat_send_failedFlag) {
            recoverCatServerConn();
        }
    }

    CATCS_LEAVE(g_server_lock);
    return 0;
}

int updateCatServerConn() {
    static int isFirstConnect = 1;

    CATCS_ENTER(g_server_lock);
    int rst;

    if (isFirstConnect) {
        rst = getRouterFromServer(g_config.serverHost, g_config.serverPort, g_cat_messageManager.domain);
    } else {
        rst = getRouterFromServer(g_cat_send_ip, g_config.serverPort, g_cat_messageManager.domain);
    }
    INNER_LOG(CLOG_DEBUG, "Get router from server successfully");

    if (isFirstConnect || rst > 0) {
        isFirstConnect = 0;

        updateCatActiveConnIndex();

        INNER_LOG(CLOG_DEBUG, "Trying connecting to the best server");
        if (tryConnBestServer() == 0) {
            g_cat_send_failedFlag = 1;
            CATCS_LEAVE(g_server_lock);
            return 0;
        } else {
            g_cat_send_failedFlag = 0;
            CATCS_LEAVE(g_server_lock);
            return 1;
        }
    }

    g_cat_send_failedFlag = 1;
    CATCS_LEAVE(g_server_lock);
    return 0;
}
