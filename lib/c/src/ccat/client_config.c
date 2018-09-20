#include "client_config.h"

#include <lib/cat_clog.h>
#include <lib/cat_ezxml.h>
#include <lib/cat_anet.h>

#include "message_aggregator.h"

CatClientInnerConfig g_config;

extern int g_log_permissionOpt;
extern int g_log_debug;
extern int g_log_saveFlag;
extern int g_log_file_with_time;
extern int g_log_file_perDay;

volatile int g_cat_enabledFlag = 0;

void catChecktPtrWithName(void *ptr, char *ptrName) {
    if (ptr == NULL) {
        INNER_LOG(CLOG_ERROR, "memory allocation failed. (oom).", ptrName);
        logError("Error", "OutOfMemory");
    }
}

inline int isCatEnabled() {
    return g_cat_enabledFlag;
}

int loadCatClientConfig(const char *filename) {
    FILE *file = fopen(filename, "r");
    if (file == NULL) {
        INNER_LOG(CLOG_WARNING, "File %s not exists.", filename);
        INNER_LOG(CLOG_WARNING, "client.xml is required to initialize cat client!");
        return -1;
    }

    int serverIndex = 0;
    ezxml_t f1 = ezxml_parse_file(filename), servers, server;

    for (servers = ezxml_child(f1, "servers"); servers; servers = servers->next) {

        for (server = ezxml_child(servers, "server"); server; server = server->next) {
            const char *ip;
            ip = ezxml_attr(server, "ip");

            if (ip != NULL && ip[0] != '\0') {
                if (serverIndex == 0) {
                    g_config.serverHost = catsdsnew(ip);
                }
                if (serverIndex > g_config.serverNum) {
                    g_config.serverAddresses = (sds *) realloc(g_config.serverAddresses,
                                                               sizeof(sds) * (serverIndex + 1));
                    g_config.serverAddresses[serverIndex] = catsdscpy(g_config.serverAddresses[serverIndex], ip);
                    g_config.serverAddresses[serverIndex] = catsdscat(g_config.serverAddresses[serverIndex], ":2280");
                } else {
                    g_config.serverAddresses[serverIndex] = catsdscat(catsdsnew(ip), ":2280");
                }
                serverIndex++;
            }
        }

    }
    ezxml_free(f1);

    if (serverIndex <= 0) {
        return 0;
    }

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
    return 1;
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
    g_config.dataDir = catsdsnew(DEFAULT_DATA_DIR);

    g_config.indexFileName = catsdsnew("client.idx.h");

    g_config.encoderType = config->encoderType;
    g_config.enableHeartbeat = config->enableHeartbeat;
    g_config.enableSampling = config->enableSampling;
    g_config.enableMultiprocessing = config->enableMultiprocessing;
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

