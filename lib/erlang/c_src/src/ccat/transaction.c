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
#include "message.h"

#include "ccat/client_config.h"
#include "ccat/message_manager.h"
#include "ccat/message_helper.h"

#include "lib/cat_clog.h"
#include "lib/cat_time_util.h"

extern sds g_multiprocessing_pid_str;

static void addDataPairNull(CatTransaction *message, const char *data) {}

static void addKVNull(CatTransaction *message, const char *dataKey, const char *dataValue) {}

static void setStatusNull(CatTransaction *message, const char *status) {}

static void setTimestampNull(CatTransaction *message, unsigned long long timestamp) {}

static void setCompleteNull(CatTransaction *message) {}

static void addChildNull(CatTransaction *message, CatMessage *childMsg) {}

static void setDurationInMillisNull(CatTransaction *trans, unsigned long long duration) {}

static void setDurationStartNull(CatTransaction *trans, unsigned long long durationStart) {}

CatTransaction g_cat_nullTrans = {
        addDataPairNull,
        addKVNull,
        setStatusNull,
        setTimestampNull,
        setCompleteNull,
        addChildNull,
        setDurationInMillisNull,
        setDurationStartNull,
};

static void addChild(CatTransaction *message, CatMessage *childMsg) {

    CatTransactionInner *pInner = getInnerTrans(message);
    int pushRst = pushBackCATStaticQueue(pInner->children, childMsg);
    if (CATSTATICQUEUE_ERR == pushRst) {
        INNER_LOG(CLOG_ERROR, "Transaction Add Child Error！%d", getCATStaticQueueSize(pInner->children));
    }
}

void *clearTransaction(CatMessage *message) {
    CatTransactionInner *pInner = getInnerTrans(message);
    clearMessage(message);
    size_t i = 0;
    for (; i < getCATStaticQueueSize(pInner->children); ++i) {
        CatMessage *pMessage = getCATStaticQueueByIndex(pInner->children, i);
        deleteCatMessage(pMessage);
    }
    destroyCATStaticQueue(pInner->children);
    return pInner;
}

static void setTransactionComplete(CatTransaction *message) {
    CatTransactionInner *pInner = getInnerTrans(message);
    if (pInner->message.isComplete) {
        // do nothing.
    } else {
        if (pInner->durationUs == 0) {
            pInner->durationUs = GetTime64() * 1000 - pInner->durationStart / 1000;
        }
        pInner->message.isComplete = 1;
        catMessageManagerEndTrans((CatTransaction *) message);
    }
}

CATStaticQueue *getCatTransactionChildren(CatTransaction *pSrcTrans) {
    CatTransactionInner *pInner = getInnerTrans(pSrcTrans);
    return pInner->children;
}

static void setDurationInMillis(CatTransaction *trans, unsigned long long duration) {
    setCatTransactionDurationUs(trans, duration * 1000);
}

static void setDurationStart(CatTransaction *trans, unsigned long long start) {
    CatTransactionInner *tInner = getInnerTrans(trans);
    tInner->durationStart = start * 1000 * 1000;
}

CatTransaction *createCatTransaction(const char *type, const char *name) {
    CatTransactionInner *pTransInner = malloc(sizeof(CatTransaction) + sizeof(CatTransactionInner));
    if (pTransInner == NULL) {
        return NULL;
    }

    CatTransaction *pTrans = (CatTransaction *) (((char *) pTransInner + sizeof(CatTransactionInner)));
    initCatMessage((CatMessage *) pTrans, CatMessageType_Trans, type, name);
    pTransInner->children = createCATStaticQueue(g_config.maxChildSize);
    pTransInner->durationStart = GetTime64() * 1000 * 1000;
    pTransInner->durationUs = 0;

    pTrans->complete = setTransactionComplete;
    pTrans->addChild = addChild;
    pTrans->setDurationInMillis = setDurationInMillis;
    pTrans->setDurationStart = setDurationStart;

    pTrans->setStatus(pTrans, CAT_SUCCESS); // status default success
    return pTrans;
}

unsigned long long getCatTransactionDurationUs(CatTransaction *trans) {
    CatTransactionInner *pInner = getInnerTrans(trans);

    if (pInner->durationUs > 0) {
        return pInner->durationUs;
    } else {
        unsigned long long tmpDuration = 0;
        size_t len = pInner->children == NULL ? 0 : getCATStaticStackSize(pInner->children);

        if (len > 0 && pInner->children != NULL) {
            CatMessage *lastChild = getCATStaticStackByIndex(pInner->children, len - 1);
            CatMessageInner *lastChildInner = getInnerMsg(lastChild);

            if (isCatTransaction(lastChild)) {
                CatTransactionInner *pInner = getInnerTrans(lastChild);
                tmpDuration = (lastChildInner->timestampMs - pInner->message.timestampMs) * 1000 + pInner->durationUs;
            } else {
                tmpDuration = (lastChildInner->timestampMs - pInner->message.timestampMs) * 1000;
            }
        }
        return tmpDuration;
    }

}

CatTransaction *copyCatTransaction(CatTransaction *pSrcTrans) {
    CatTransactionInner *pSrcTransInner = getInnerTrans(pSrcTrans);
    CatTransaction *clonedTrans = createCatTransaction(pSrcTransInner->message.type, pSrcTransInner->message.name);
    CatTransactionInner *clonedTransInner = getInnerTrans(clonedTrans);
    clonedTransInner->message.timestampMs = pSrcTransInner->message.timestampMs;
    clonedTransInner->durationUs = getCatTransactionDurationUs(pSrcTrans);
    clonedTransInner->message.data = catsdsdup(pSrcTransInner->message.data);
    return clonedTrans;
}

