#include "router_json_parser.h"

#include "functions.h"
#include "message_aggregator.h"

#include <lib/cat_json.h>
#include <lib/cat_clog.h>

#define ROUTER_CONFIG_ROUTERS "routers"
#define ROUTER_CONFIG_SAMPLE_RATE "sample"
#define ROUTER_CONFIG_BLOCK "block"

extern volatile int g_cat_enabledFlag;

extern int resolveServerIps(char *routerIps);

static int parseJsonStringArray(cJSON *kvsObject, char *objName, int *pNum, sds **pStrArray, char *splitor) {
    cJSON *item = NULL;
    char *itemBuf = NULL;
    item = cJSON_GetObjectItem(kvsObject, objName);
    if (item != NULL && (itemBuf = item->valuestring) != NULL) {
        *pStrArray = catsdssplitlen(itemBuf, strlen(itemBuf), splitor, strlen(splitor), pNum);

        if (*pStrArray != NULL && *pNum > 0) {
            return 1;
        }
    } else {
        INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem Error, no key [%s].", objName);
    }
    return 0;
}

static int parseJsonDouble(cJSON *kvsObject, char *objName, double *pVal) {
    cJSON *item = NULL;
    char *itemBuf = NULL;
    item = cJSON_GetObjectItem(kvsObject, objName);
    if (item != NULL && (itemBuf = item->valuestring) != NULL) {
        char *endBuf = NULL;
        double val = strtod(itemBuf, &endBuf);
        if (endBuf != NULL && endBuf[0] == '\0') {
            *pVal = val;
            return 1;
        } else {
            INNER_LOG(CLOG_WARNING, "CatRouter Json strtod Error, key [%s].", objName);
        }
    } else {
        INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem Error, no key [%s].", objName);
    }
    return 0;
}

static int parsersonInt(cJSON *kvsObject, char *objName, int *pVal) {
    cJSON *item = NULL;
    char *itemBuf = NULL;
    item = cJSON_GetObjectItem(kvsObject, objName);
    if (item != NULL && (itemBuf = item->valuestring) != NULL) {
        if (!catAtoI(itemBuf, 10, pVal)) {
            INNER_LOG(CLOG_WARNING, "CatRouter Json catAtoI Error, key [%s].", objName);
        } else {
            return 1;
        }
    } else {
        INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem Error, no key [%s].", objName);
    }
    return 0;
}

static int parseJsonBool(cJSON *kvsObject, char *objName, int *pRst) {
    cJSON *item = NULL;
    char *itemBuf = NULL;
    item = cJSON_GetObjectItem(kvsObject, objName);
    if (item != NULL && (itemBuf = item->valuestring) != NULL) {
        if (strcmp(itemBuf, "true") == 0 || strcmp(itemBuf, "TRUE") == 0
            || strcmp(itemBuf, "Yes") == 0 || strcmp(itemBuf, "yes") == 0) {
            *pRst = 1;
        } else {
            *pRst = 0;
        }
        return 1;
    } else {
        INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem Error, no key [%s].", objName);
    }
    return 0;
}

static int parseCatJsonRouterItem(cJSON *kvsObject) {
    int rst = 0;

    cJSON *item = NULL;
    char *itemBuf = NULL;

    item = cJSON_GetObjectItem(kvsObject, ROUTER_CONFIG_ROUTERS);
    if (item != NULL && (itemBuf = item->valuestring) != NULL) {
        if (resolveServerIps(itemBuf) > 0) {
            rst += 1;
        } else {
            INNER_LOG(CLOG_WARNING, "CatRouter Json catAtoI Error, key [routers].");
        }
    } else {
        INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem Error, no key [routers].");
    }

    double sampleRate = 1.0;
    parseJsonDouble(kvsObject, ROUTER_CONFIG_SAMPLE_RATE, &sampleRate);
    setSampleRate(sampleRate);

    int block = 0;
    parseJsonBool(kvsObject, ROUTER_CONFIG_BLOCK, &block);
    g_cat_enabledFlag = !block;

    return rst;
}

int parseCatJsonRouter(char *jsonBuf) {
    cJSON *catRouterJson = NULL;

    catRouterJson = cJSON_Parse(jsonBuf);
    if (catRouterJson == NULL) {
        INNER_LOG(CLOG_WARNING, "CatRouter Json Parser Error before: [%s]\n", cJSON_GetErrorPtr());
        return 0;
    } else {
        cJSON *kvsItem = cJSON_GetObjectItem(catRouterJson, "kvs");
        if (kvsItem == NULL) {
            INNER_LOG(CLOG_WARNING, "CatRouter Json GetObjectItem [kvs] Error before: [%s]\n", cJSON_GetErrorPtr());
            return 0;
        }
        if (cJSON_GetArraySize(kvsItem) < 1) {
            INNER_LOG(CLOG_WARNING, "CatRouter Json ArraySize [kvs] Error before: [%s]\n", cJSON_GetErrorPtr());
            return 0;
        }

        parseCatJsonRouterItem(kvsItem);
        cJSON_Delete(catRouterJson);
    }
    return 1;
}
