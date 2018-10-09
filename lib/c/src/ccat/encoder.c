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

#include "lib/cat_clog.h"

CatEncoder *g_cat_encoder = NULL;

void _CatEncodeMessage(CatEncoder *encoder, CatMessage *message) {
    if (isCatTransaction(message)) {
        encoder->transaction(encoder, (CatTransaction *) message);
    } else if (isCatEvent(message)) {
        encoder->event(encoder, message);
    } else if (isCatMetric(message)) {
        encoder->metric(encoder, message);
    } else if (isCatHeartBeat(message)) {
        encoder->heartbeat(encoder, message);
    } else {
        INNER_LOG(CLOG_ERROR, "Unsupported message type: %s.", getCatMessageType(message));
    }
}

void _CatEncodeTransaction(CatEncoder *encoder, CatTransaction *transaction) {
    g_cat_encoder->transactionStart(g_cat_encoder, transaction);
    CATStaticQueue *children = getCatTransactionChildren(transaction);
    size_t len = getCATStaticStackSize(children);

    size_t i;
    for (i = 0; i < len; i++) {
        CatMessage *child = getCATStaticStackByIndex(children, i);
        if (child != NULL) {
            _CatEncodeMessage(encoder, child);
        }
    }
    g_cat_encoder->transactionEnd(g_cat_encoder, transaction);
}

void _CatEncoderSetAppkey(CatEncoder *encoder, const char *appkey) {
    encoder->appkey = strdup(appkey);
}

void _CatEncoderSetHostname(CatEncoder *encoder, const char *hostname) {
    encoder->hostname = strdup(hostname);
}

void _CatEncoderSetIp(CatEncoder *encoder, const char *ip) {
    encoder->ip = strdup(ip);
}

CatEncoder *newCatEncoder() {
    CatEncoder *encoder = malloc(sizeof(CatEncoder));

    encoder->appkey = strdup("cat");
    encoder->hostname = strdup("localhost");
    encoder->ip = strdup("127.0.0.1");

    encoder->setAppkey = _CatEncoderSetAppkey;
    encoder->setHostname = _CatEncoderSetHostname;
    encoder->setIp = _CatEncoderSetIp;

    encoder->message = _CatEncodeMessage;
    encoder->transaction = _CatEncodeTransaction;

    return encoder;
}

void deleteCatEncoder(CatEncoder *encoder) {
    free((void *) encoder->appkey);
    free((void *) encoder->hostname);
    free((void *) encoder->ip);
    free(encoder);
}

void catEncodeMessageTree(CatMessageTree *tree, sds buf) {
    if (NULL == g_cat_encoder) {
        INNER_LOG(CLOG_ERROR, "Global message encoder has not been initialized!");
        return;
    }

    size_t pos = catsdslen(buf);
    buf = catsdscatlen(buf, "0000", 4);
    g_cat_encoder->buf = &buf;

    // encode message messageType.
    g_cat_encoder->header(g_cat_encoder, tree);

    // encode message body if exists.
    if (tree->root != NULL) {
        g_cat_encoder->message(g_cat_encoder, tree->root);
    }

    size_t length = catsdslen(buf) - pos - 4;
    buf[pos++] = (char) ((length >> 24) & 0xFF);
    buf[pos++] = (char) ((length >> 16) & 0xFF);
    buf[pos++] = (char) ((length >> 8) & 0xFF);
    buf[pos] = (char) ((length) & 0xFF);
}
