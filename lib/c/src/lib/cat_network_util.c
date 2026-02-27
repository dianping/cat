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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef WIN32

// TODO win32 header files.

#else

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <net/if.h>
#include <netdb.h>
#include <unistd.h>

#define    NI_MAXHOST       1025
#define    NI_NUMERICHOST   0x00000002
#define    IFF_UP           0x1
#define    IFF_LOOPBACK     0x8

#endif

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

#ifndef WIN32

int getLocalHostIp(char *ip) {
    struct ifaddrs *ifaddrs = NULL;

    if (getifaddrs(&ifaddrs)) {
        return -1;
    }

    struct in_addr res = {0};

    int res_level = 0, tmp_level = 0;

    char ipBuf[64];

    struct ifaddrs *ifa;
    for (ifa = ifaddrs; NULL != ifa; ifa = ifa->ifa_next) {
        char hostname[NI_MAXHOST];
        if (ifa->ifa_addr == NULL) {
            continue;
        }
        if (ifa->ifa_addr->sa_family == AF_INET) {

            // ignore not up interface.
            if (!(ifa->ifa_flags & IFF_UP)) {
                continue;
            }

            // ignore loopback interface.
            if (ifa->ifa_flags & IFF_LOOPBACK) {
                continue;
            }

            // get hostname and ip
            getnameinfo(ifa->ifa_addr, sizeof(struct sockaddr_in), hostname, NI_MAXHOST, NULL, 0, NI_NUMERICHOST);

            struct in_addr *addr = &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr;
            if (NULL == inet_ntop(AF_INET, addr, ipBuf, 16)) {
                continue;
            }

            // we put interface address with a hostname in a higher priority.
            int offset = strcmp(ipBuf, hostname) == 0 ? 0 : 1;

            if (0 == res.s_addr) {
                res = *addr;
                res_level = ipAddressLevel(&res, offset);
            } else if (res_level < (tmp_level = ipAddressLevel(addr, offset))) {
                res = *addr;
                res_level = tmp_level;
            }
        }
    }
    freeifaddrs(ifaddrs);

    if (0 == res.s_addr) {
        return -1;
    } else {
        return NULL == inet_ntop(AF_INET, &res, ip, 16) ? -1 : 0;
    }
}

#else

int getLocalHostIp(char *ip) {
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
}

#endif

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


