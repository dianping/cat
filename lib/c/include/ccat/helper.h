//
// Created by Terence on 2018/8/23.
//

#ifndef CCAT_HELPER_H
#define CCAT_HELPER_H

#include "client.h"

typedef struct _CatHelper {
    void (*init)(const char *appkey);

    void (*newTransaction)(const char *type, const char *name);

    void (*newEvent)(const char *type, const char *name);

    void (*newHeartbeat)(const char *type, const char *name);

    void (*logEvent)(const char *type, const char *name, const char *status, const char *data);

    void (*logError)(const char *msg, const char *errStr);

    void (*logMetricForCount)(const char *key, int quantity);

    void (*logMetricForCountQuantity)(const char *name, int quantity);

    void (*logMetricForDuration)(const char *name, unsigned long long durationMs);

    void (*newMetricHelper)(const char *type, const char *name);
} CatHelper;

CAT_CLIENT_EXPORT CatHelper *cat();

#endif //CCAT_HELPER_H
