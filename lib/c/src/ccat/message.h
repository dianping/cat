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
#ifndef CCAT_MESSAGE_H
#define CCAT_MESSAGE_H

#include <string.h>
#include <stdlib.h>

#include "client.h"
#include "ccat/message_helper.h"

#include "lib/cat_sds.h"
#include "lib/cat_static_queue.h"

#define CatMessageType_Trans 'T'
#define CatMessageType_Event 'E'
#define CatMessageType_HeartBeat 'H'
#define CatMessageType_Metric 'M'

typedef struct _CatMessageInner {
    union {
        char type;
        int flag;
    } messageType;

    sds type;
    sds name;
    sds status;
    sds data;
    unsigned long long timestampMs; // created time

    int isComplete;
} CatMessageInner;

typedef struct _CatTranscationInner {
    CATStaticQueue *children;
    unsigned long long durationStart;     // nanoseconds
    unsigned long long durationUs;        // microseconds
    CatMessageInner message;
} CatTransactionInner;

#define getInnerMsg(pMsg) ((CatMessageInner*)(((char*)(pMsg)) - sizeof(CatMessageInner)))

#define getInnerTrans(pMsg) ((CatTransactionInner*)(((char*)(pMsg)) - sizeof(CatTransactionInner)))

void *clearMessage(CatMessage *message);

void *clearTransaction(CatMessage *message);

static inline void deleteCatMessage(CatMessage *message) {
    void* p;
    if (isCatTransaction(message)) {
        p = clearTransaction(message);
    } else {
        p = clearMessage(message);
    }
    free(p);
}

static inline int isCatMessageComplete(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->isComplete;
}

static inline unsigned long long getCatMessageTimeStamp(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->timestampMs;
}

static inline sds getCatMessageType(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->type;
}

static inline int checkCatMessageSuccess(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    if (pInner->status == NULL || catsdslen(pInner->status) != 1 || pInner->status[0] != '0') {
        return 0;
    }
    return 1;
}

void initCatMessage(CatMessage *pMsg, char msgType, const char *type, const char *name);

CatTransaction *createCatTransaction(const char *type, const char *name);

CatTransaction *copyCatTransaction(CatTransaction *pSrcTrans);

unsigned long long getCatTransactionDurationUs(CatTransaction *trans);

static void inline setCatTransactionDurationUs(CatTransaction *trans, unsigned long long durationUs) {
    CatTransactionInner *pInner = getInnerTrans(trans);
    pInner->durationUs = durationUs;
}

CATStaticQueue *getCatTransactionChildren(CatTransaction *pSrcTrans);

CatMetric *newMetric(const char *type, const char *name);

CatEvent *createCatEvent(const char *type, const char *name);

CatMetric *createCatMetric(const char *type, const char *name);

CatHeartBeat *createCatHeartBeat(const char *type, const char *name);

#endif //CCAT_MESSAGE_H
