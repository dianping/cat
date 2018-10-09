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

#define CAT_ENCODER_VERSION "NT1"

static inline sds sdswritelong(sds s, unsigned long long val) {
    while (1) {
        if ((val & ~0x7FL) == 0) {
            return catsdscatchar(s, (char) val);
        } else {
            s = catsdscatchar(s, (char) ((val & 0x7F) | 0x80));
            val >>= 7;
        }
    }
}

static inline sds sdswritestringwithnull(sds s, const char *buf) {
    if (buf == NULL) {
        return sdswritelong(s, 0);
    } else {
        size_t len = strlen(buf);
        s = sdswritelong(s, len);
        return catsdscatlen(s, buf, len);
    }
}

static inline sds sdscatwithnull(sds s, const char *buf) {
    return catsdscat(s, buf == NULL ? "null" : buf);
}

static inline sds sdscatwithdefault(sds s, const char *buf, const char *defaultStr) {
    return catsdscat(s, buf == NULL ? defaultStr : buf);
}


static inline void catBinaryTransactionStart(CatEncoder *encoder, CatTransaction *transaction) {
    sds tmpBuf = *encoder->buf;
    CatTransactionInner *pTransInner = getInnerTrans(transaction);
    tmpBuf = catsdscatchar(tmpBuf, 't');
    tmpBuf = sdswritelong(tmpBuf, getCatMessageTimeStamp(transaction));
    tmpBuf = sdswritestringwithnull(tmpBuf, pTransInner->message.type);
    tmpBuf = sdswritestringwithnull(tmpBuf, pTransInner->message.name);
    *encoder->buf = tmpBuf;
}

static inline void catBinaryTransactionEnd(CatEncoder *encoder, CatTransaction *transaction) {
    sds tmpBuf = *encoder->buf;
    CatTransactionInner *pTransInner = getInnerTrans(transaction);
    tmpBuf = catsdscatchar(tmpBuf, 'T');
    tmpBuf = sdswritestringwithnull(tmpBuf, pTransInner->message.status);
    tmpBuf = sdswritestringwithnull(tmpBuf, pTransInner->message.data);
    tmpBuf = sdswritelong(tmpBuf, getCatTransactionDurationUs(transaction));
    *encoder->buf = tmpBuf;
}

static inline void catBinaryEvent(CatEncoder *encoder, CatEvent *event) {
    sds tmpBuf = *encoder->buf;
    CatMessageInner *pMsgInner = getInnerMsg(event);
    tmpBuf = catsdscatchar(tmpBuf, 'E');
    tmpBuf = sdswritelong(tmpBuf, getCatMessageTimeStamp(event));
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->type);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->name);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->status);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->data);
    *encoder->buf = tmpBuf;
}

static inline void catBinaryMetric(CatEncoder *encoder, CatMetric *metric) {
    sds tmpBuf = *encoder->buf;
    CatMessageInner *pMsgInner = getInnerMsg(metric);
    tmpBuf = catsdscatchar(tmpBuf, 'M');
    tmpBuf = sdswritelong(tmpBuf, getCatMessageTimeStamp(metric));
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->type);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->name);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->status);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->data);
    *encoder->buf = tmpBuf;
}

static inline void catBinaryHeartbeat(CatEncoder *encoder, CatHeartBeat *heartbeat) {
    sds tmpBuf = *encoder->buf;
    CatMessageInner *pMsgInner = getInnerMsg(heartbeat);
    tmpBuf = catsdscatchar(tmpBuf, 'H');
    tmpBuf = sdswritelong(tmpBuf, getCatMessageTimeStamp(heartbeat));
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->type);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->name);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->status);
    tmpBuf = sdswritestringwithnull(tmpBuf, pMsgInner->data);
    *encoder->buf = tmpBuf;
}

void catBinaryEncodeHeader(CatEncoder *encoder, CatMessageTree *tree) {
    sds tmpBuf = *encoder->buf;
    tmpBuf = sdscatwithnull(tmpBuf, CAT_ENCODER_VERSION);
    tmpBuf = sdswritestringwithnull(tmpBuf, encoder->appkey);
    tmpBuf = sdswritestringwithnull(tmpBuf, encoder->hostname);
    tmpBuf = sdswritestringwithnull(tmpBuf, encoder->ip);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->threadGroupName);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->threadId);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->threadName);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->messageId);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->parentMessageId);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->rootMessageId);
    tmpBuf = sdswritestringwithnull(tmpBuf, tree->sessionToken);
    *encoder->buf = tmpBuf;
}

CatEncoder *newCatBinaryEncoder() {
    CatEncoder *encoder = newCatEncoder();
    encoder->header = catBinaryEncodeHeader;
    encoder->transactionStart = catBinaryTransactionStart;
    encoder->transactionEnd = catBinaryTransactionEnd;
    encoder->event = catBinaryEvent;
    encoder->metric = catBinaryMetric;
    encoder->heartbeat = catBinaryHeartbeat;
    return encoder;
}
