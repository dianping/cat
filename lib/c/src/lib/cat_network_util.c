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
#include "cat_network_util.h"

#include "lib/headers.h"

int ipAddressLevel(struct in_addr *addr, int offset) {
    uint32_t a = addr->s_addr;
    if ((a & 0xFF) == 10) {
        return 1 + offset;
    } else if (((a & 0xFF) == 172) && ((a >> 8 & 0xF0) == 16)) {
        return 1 + offset;
    } else if (((a & 0xFF) == 192) && ((a >> 8 & 0xFF) == 168)) {
        return 1 + offset;
    } else {
        return 3 + offset;
    }
}

int getLocalHostIp(char *ip) {

#ifndef WIN32

    struct ifaddrs *interfaces = NULL;
    if (getifaddrs(&interfaces)) {
        return -1;
    }

    struct in_addr *res = NULL;
    int res_level = 0, tmp_level = 0;

    struct ifaddrs *tmp;
    for (tmp = interfaces; NULL != tmp; tmp = tmp->ifa_next) {
        char ipbuf[64];
        char hostname[NI_MAXHOST];

        if (tmp->ifa_addr->sa_family == AF_INET) {

            // ignore not up interface.
            if (!(tmp->ifa_flags & IFF_UP)) {
                continue;
            }

            // ignore loopback interface.
            if (tmp->ifa_flags & IFF_LOOPBACK) {
                continue;
            }

            // get hostname and ip
            getnameinfo(tmp->ifa_addr, sizeof(struct sockaddr_in), hostname, NI_MAXHOST, NULL, 0, NI_NUMERICHOST);
            struct in_addr *addr = &((struct sockaddr_in *) tmp->ifa_addr)->sin_addr;
            if (NULL == inet_ntop(AF_INET, addr, ipbuf, 16)) {
                continue;
            }
            // we put interface address with a hostname in a higher priority.
            int offset = strcmp(ipbuf, hostname) == 0 ? 0 : 1;

            if (NULL == res) {
                res = addr;
                res_level = ipAddressLevel(res, offset);
            } else if (res_level < (tmp_level = ipAddressLevel(addr, offset))) {
                res = addr;
                res_level = tmp_level;
            }
        }
    }
    freeifaddrs(interfaces);

    if (NULL == res) {
        return -1;
    } else {
        return NULL == inet_ntop(AF_INET, res, ip, 16) ? -1 : 0;
    }

#else

    char hostname[255];
    PHOSTENT hostinfo = NULL;
    u_int32 ipValue = 0;
    if (gethostname(hostname, sizeof(hostname)) == 0)
    {
        if ((hostinfo = gethostbyname(hostname)) != NULL)
        {
            memcpy(&ipValue, *hostinfo->h_addr_list, 4);
        }
    }
    if (ipValue == 0)
    {
        return -1;
    }
    struct in_addr inaddr;
    inaddr.s_addr = ipValue;
    strcpy(ipBuf, inet_ntoa(inaddr));
    return 0;

#endif

}

int getLocalHostName(char *host, size_t bufLen) {
    if (host != NULL) {
        if (gethostname(host, bufLen) == 0) {
            printf("host name : %s \n", host);
            return 0;
        } else {
            printf("Get HostName Error \n");
        }
    }
    return -1;
}

int hostnameToIp(char *hostname, char *ip) {
    struct hostent *he;
    struct in_addr **addr_list;
    int i;

    if ((he = gethostbyname(hostname)) == NULL) {
        // get the host info
        herror("get host by name fail");
        return -1;
    }

    addr_list = (struct in_addr **) he->h_addr_list;

    for (i = 0; addr_list[i] != NULL; i++) {
        //Return the first one;
        strcpy(ip, inet_ntoa(*addr_list[i]));
        return 0;
    }

    return -1;
}

int getLocalHostIpHex(char *ipHexBuf) {
    char ip[64] = {0};

    if (getLocalHostIp(ip) < 0 || ip[0] == '\0') {
        return -1;
    }

    int a[4];
    sscanf(ip, "%d.%d.%d.%d", &a[0], &a[1], &a[2], &a[3]);
    sprintf(ipHexBuf, "%02x%02x%02x%02x", a[0], a[1], a[2], a[3]);
    return 0;
}


