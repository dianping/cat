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
#include "client_config.h"
#include <stdlib.h>

#include <lib/cat_clog.h>
#include <lib/cat_ezxml.h>
#include <lib/cat_anet.h>

#include "aggregator.h"

CatClientInnerConfig g_config;

extern int g_log_permissionOpt;
extern int g_log_debug;
extern int g_log_saveFlag;
extern int g_log_file_with_time;
extern int g_log_file_perDay;

volatile int g_cat_enabled = 0;

void catChecktPtrWithName(void *ptr, char *ptrName) {
    if (ptr == NULL) {
        INNER_LOG(CLOG_ERROR, "memory allocation failed. (oom).", ptrName);
        logError("Error", "OutOfMemory");
    }
}

inline int isCatEnabled() {
    return g_cat_enabled;
}

static ezxml_t getCatClientConfig(const char *filename) {
    FILE *file = fopen(filename, "r");
    if (file == NULL) {
        return NULL;
    } else {
        return ezxml_parse_file(filename);
    }
}

int parseCatClientConfig(ezxml_t f1) {
    int serverIndex = 0;
    ezxml_t servers, server;

    for (servers = ezxml_child(f1, "servers"); servers; servers = servers->next) {
        for (server = ezxml_child(servers, "server"); server; server = server->next) {
            const char *ip;
            ip = ezxml_attr(server, "ip");

            if (NULL == ip || ip[0] == '\0') {
                continue;
            }

            if (serverIndex == 0) {
                const char * port;

                g_config.serverHost = catsdsnew(ip);
                
                port = ezxml_attr(server, "http-port");
                if (port && port[0] != '\0') {
                    g_config.serverPort = atoi(port);
                }
                
            } else if (serverIndex >= g_config.serverNum) {
                break;
            }
            serverIndex++;
        }
    }
    ezxml_free(f1);

    if (serverIndex <= 0) {
        return -1;
    }
    return 0;
}

int loadCatClientConfig(const char *filename) {
    ezxml_t config = getCatClientConfig(filename);
    if (NULL == config) {
        INNER_LOG(CLOG_WARNING, "File %s not exists.", filename);
        INNER_LOG(CLOG_WARNING, "client.xml is required to initialize cat client!");
        return -1;
    }

    if (parseCatClientConfig(config) < 0) {
        INNER_LOG(CLOG_ERROR, "Failed to parse client.xml, is it a legal xml file?");
        return -1;
    }
    return 0;
}

void initCatClientConfig(CatClientConfig *config) {
    memset(&g_config, 0, sizeof(g_config));

    g_log_debug = config->enableDebugLog;
    _CLog_debugInfo("encoder: %d\n", config->encoderType);
    _CLog_debugInfo("sampling: %d\n", config->enableSampling);
    _CLog_debugInfo("multiprocessing: %d\n", config->enableMultiprocessing);
    _CLog_debugInfo("heartbeat: %d\n", config->enableHeartbeat);

    g_config.appkey = DEFAULT_APPKEY;
    g_config.selfHost = catsdsnewEmpty(128);

    g_config.defaultIp = catsdsnew(DEFAULT_IP);
    g_config.defaultIpHex = catsdsnew(DEFAULT_IP_HEX);

    if (catAnetGetHost(NULL, g_config.selfHost, 128) == ANET_ERR) {
        g_config.selfHost = catsdscpy(g_config.selfHost, "CUnknownHost");
    }
    INNER_LOG(CLOG_INFO, "Current hostname: %s", g_config.selfHost);

    g_config.serverHost = catsdsnew(DEFAULT_IP);
    g_config.serverPort = 8080;
    g_config.serverNum = 3;
    g_config.serverAddresses = (sds *) malloc(g_config.serverNum * sizeof(sds));

    int i = 0;
    for (i = 0; i < g_config.serverNum; ++i) {
        g_config.serverAddresses[i] = catsdsnew("");
    }

    g_config.serverAddresses[0] = catsdscpy(g_config.serverAddresses[0], "127.0.0.1:2280");
    g_config.serverAddresses[1] = catsdscpy(g_config.serverAddresses[1], "127.0.0.1:2280");
    g_config.serverAddresses[2] = catsdscpy(g_config.serverAddresses[2], "127.0.0.1:2280");

    g_config.messageEnableFlag = 1;
    g_config.messageQueueSize = 10000;
    g_config.messageQueueBlockPrintCount = 100000;
    g_config.maxContextElementSize = 2000;
    g_config.maxChildSize = 2048;

    g_config.logFlag = 1;
    g_config.logSaveFlag = 1;
    g_config.logDebugFlag = config->enableDebugLog;
    g_config.logFilePerDay = 1;
    g_config.logFileWithTime = 0;
    g_config.logLevel = CLOG_ALL;

    g_config.configDir = catsdsnew("./");

    // CAT_HOME
    char *cathomevar;
    cathomevar = catHome();
    const char *dataDir = cathomevar;
    printf("Using dataDir=%s\n", dataDir);
    g_config.dataDir = catsdsnew(dataDir);

    g_config.indexFileName = catsdsnew("client.idx.h");

    g_config.encoderType = config->encoderType;
    g_config.enableHeartbeat = config->enableHeartbeat;
    g_config.enableSampling = config->enableSampling;
    g_config.enableMultiprocessing = config->enableMultiprocessing;
    g_config.enableAutoInitialize = config->enableAutoInitialize;

    // logging configs
    if (!g_config.logFlag) {
        g_log_permissionOpt = 0;
    } else {
        g_log_permissionOpt = g_config.logLevel;
        g_log_saveFlag = g_config.logLevel;
        g_log_file_perDay = g_config.logFilePerDay;
        g_log_file_with_time = g_config.logFileWithTime;
        g_log_debug = g_config.logDebugFlag;
    }
}

void clearCatClientConfig() {
    catsdsfree(g_config.appkey);
    catsdsfree(g_config.selfHost);

    catsdsfree(g_config.defaultIp);
    catsdsfree(g_config.defaultIpHex);

    catsdsfree(g_config.serverHost);
    int i = 0;
    for (i = 0; i < g_config.serverNum; ++i) {
        catsdsfree(g_config.serverAddresses[i]);
    }
    free(g_config.serverAddresses);

    catsdsfree(g_config.configDir);
    catsdsfree(g_config.dataDir);
    catsdsfree(g_config.indexFileName);
}


char *catHome() {
       // CAT_HOME
    char *cathomevar;
    cathomevar = getenv("CAT_HOME");
    if (cathomevar == NULL) {
        cathomevar = DEFAULT_CAT_HOME;
    }
    return cathomevar;
}
