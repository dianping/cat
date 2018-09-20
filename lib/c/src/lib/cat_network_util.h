#ifndef CAT_CLIENT_C_NETWORK_UTIL_H
#define CAT_CLIENT_C_NETWORK_UTIL_H

#include "cat_sds.h"

int getLocalHostIp(char *ipBuf);

int getLocalHostName(char *host, size_t bufLen);

int hostnameToIp(char *hostname, char *ip);

int getLocalHostIpHex(char *ipHexBuf);


#endif //CAT_CLIENT_C_NETWORK_UTIL_H
