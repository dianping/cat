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

/**
 * NOTE it is only a proposal, interfaces defined here has not been implemented.
 */
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
