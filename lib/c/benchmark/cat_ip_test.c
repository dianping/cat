//
// Created by Terence on 2018/8/27.
//

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <memory.h>
#include <net/if.h>
#include <netdb.h>
#include <stdio.h>

#define NULL 0

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
}

int main() {
    char ip[16];
    getLocalHostIp(ip);
    printf("%s", ip);
}