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
#include "encoder.h"

#include "ccat/message.h"
#include "ccat/message_helper.h"

#include "lib/cat_time_util.h"

#define POLICY_DEFAULT 0
#define POLICY_WITHOUT_STATUS 1
#define POLICY_WITH_DURATION 2

#define CAT_ENCODE_VERSION "PT1"
#define CAT_TAB '\t'
#define CAT_ENDL '\n'


static inline sds sdscatwithnull(sds s, const char *buf) {
    return catsdscat(s, buf == NULL ? "null" : buf);
}

static inline sds sdscatwithdefault(sds s, const char *buf, const char *defaultStr) {
    return catsdscat(s, buf == NULL ? defaultStr : buf);
}

void catTextEncodeHeader(CatEncoder *encoder, CatMessageTree *tree) {
    sds tmpBuf = *encoder->buf;
    tmpBuf = sdscatwithnull(tmpBuf, CAT_ENCODE_VERSION);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, encoder->appkey);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, encoder->hostname);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, encoder->ip);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->threadGroupName);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->threadId);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->threadName);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->messageId);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->parentMessageId);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->rootMessageId);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, tree->sessionToken);
    tmpBuf = catsdscatchar(tmpBuf, CAT_ENDL);
    *encoder->buf = tmpBuf;
}

void catEncodeLine(CatEncoder *encoder, CatMessage *message, char type, int policy) {
    sds tmpBuf = *encoder->buf;
    CatMessageInner *pMsgInner = getInnerMsg(message);

    tmpBuf = catsdscatchar(tmpBuf, type);

    if (type == 'T' && isCatTransaction(message)) {
        unsigned long long durationMs = getCatTransactionDurationUs((CatTransaction *) message) / 1000;
        tmpBuf = catsdscatprintf(tmpBuf, "%s", GetCatTimeString(getCatMessageTimeStamp(message) + durationMs));
    } else {
        tmpBuf = catsdscatprintf(tmpBuf, "%s", GetCatTimeString(getCatMessageTimeStamp(message)));
    }

    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, pMsgInner->type);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    tmpBuf = sdscatwithnull(tmpBuf, pMsgInner->name);
    tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);

    if (policy != POLICY_WITHOUT_STATUS) {
        // Set status to "DefaultStatus" while status is null
        tmpBuf = sdscatwithdefault(tmpBuf, pMsgInner->status, "DefaultStatus");
        tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);


        if (policy == POLICY_WITH_DURATION && isCatTransaction(message)) {
            unsigned long long durationUs = getCatTransactionDurationUs((CatTransaction *) message);
            tmpBuf = catsdscatprintf(tmpBuf, "%lldus\t", durationUs);
        }

        tmpBuf = sdscatwithnull(tmpBuf, pMsgInner->data);
        tmpBuf = catsdscatchar(tmpBuf, CAT_TAB);
    }
    tmpBuf = catsdscatchar(tmpBuf, CAT_ENDL);

    *encoder->buf = tmpBuf;
}

static inline void catTextTransaction(CatEncoder *encoder, CatTransaction *transaction) {
    CATStaticQueue *children = getCatTransactionChildren(transaction);
    if (isCATStaticQueueEmpty(children)) {
        catEncodeLine(encoder, (CatMessage *) transaction, 'A', POLICY_WITH_DURATION);
    } else {
        size_t len = getCATStaticStackSize(children);
        catEncodeLine(encoder, (CatMessage *) transaction, 't', POLICY_WITHOUT_STATUS);

        size_t i;
        for (i = 0; i < len; i++) {
            CatMessage *child = getCATStaticStackByIndex(children, i);
            if (child != NULL) {
                encoder->message(encoder, child);
            }
        }
        catEncodeLine(encoder, (CatMessage *) transaction, 'T', POLICY_WITH_DURATION);
    }
}

static inline void catTextEvent(CatEncoder *encoder, CatEvent *event) {
    catEncodeLine(encoder, event, 'E', POLICY_DEFAULT);
}

static inline void catTextMetric(CatEncoder *encoder, CatMetric *metric) {
    catEncodeLine(encoder, metric, 'M', POLICY_DEFAULT);
}

static inline void catTextHeartbeat(CatEncoder *encoder, CatMetric *heartbeat) {
    catEncodeLine(encoder, heartbeat, 'H', POLICY_DEFAULT);
}

CatEncoder *newCatTextEncoder() {
    CatEncoder *encoder = newCatEncoder();
    encoder->header = catTextEncodeHeader;
    encoder->transaction = catTextTransaction;
    encoder->event = catTextEvent;
    encoder->metric = catTextMetric;
    encoder->heartbeat = catTextHeartbeat;
    return encoder;
}
