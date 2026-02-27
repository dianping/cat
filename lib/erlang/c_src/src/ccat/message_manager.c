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
#include "message_manager.h"

#include "ccat/client_config.h"
#include "ccat/context.h"
#include "ccat/message_id.h"
#include "ccat/message_sender.h"

#include "lib/cat_clog.h"
#include "lib/cat_network_util.h"

CatMessageManager g_cat_messageManager = {0}; //memset(&g_cat_messageManager, 0, sizeof(g_cat_messageManager))

void catMessageManagerAdd(CatMessage *message) {
    CatContext *ctx = getCatContext();
    ctx->addMessage(ctx, message);
}

void catMessageManagerEndTrans(CatTransaction *message) {
    CatContext *ctx = getCatContext();
    ctx->endTrans(ctx, message);

    if (isCATStaticStackEmpty(ctx->transactionStack)) {
        CatMessageTree *copiedTree = copyCatMessageTree(ctx->tree);

        if (ctx->lastTruncateTransDurationUs > 0) {
            ctx->adjustForTruncatedTrans(ctx, (CatTransaction *) copiedTree->root);
        }

        catMessageManagerFlush(copiedTree);
        ctx->reset(ctx);
    }
}

void catMessageManagerFlush(CatMessageTree *tree) {
    if (NULL == tree->messageId) {
        tree->messageId = getNextMessageId();
    }
    if (isCatSenderEnable() && g_config.messageEnableFlag) {
        sendRootMessage(tree);
    } else {
        deleteCatMessageTree(tree);
        ++g_cat_messageManager.throttleTimes;
        if (g_cat_messageManager.throttleTimes == 1 || g_cat_messageManager.throttleTimes % 1000000 == 0) {
            INNER_LOG(CLOG_WARNING, "Cat Message is throttled! Times: %d", g_cat_messageManager.throttleTimes);
        }
    }
}

void initMessageManager(const char *domain, const char *hostName) {
    g_cat_messageManager.domain = catsdsnew(domain);
    catChecktPtr(g_cat_messageManager.domain);

    g_cat_messageManager.hostname = catsdsnew(hostName);
    catChecktPtr(g_cat_messageManager.hostname);

    g_cat_messageManager.ip = catsdsnewEmpty(64);
    catChecktPtr(g_cat_messageManager.ip);
    getLocalHostIp(g_cat_messageManager.ip);

    // Determine if ip has been got successfully.
    if (g_cat_messageManager.ip[0] == '\0') {
        INNER_LOG(CLOG_WARNING, "Cannot get self ip address, using default ip: %s", g_config.defaultIp);
        g_cat_messageManager.ip = catsdscpy(g_cat_messageManager.ip, g_config.defaultIp);
    }
    INNER_LOG(CLOG_INFO, "Current ip: %s", g_cat_messageManager.ip);

    g_cat_messageManager.ipHex = catsdsnewEmpty(64);
    catChecktPtr(g_cat_messageManager.ipHex);
    getLocalHostIpHex(g_cat_messageManager.ipHex);

    // Determine if ipX has been got successfully.
    if (g_cat_messageManager.ipHex[0] == '\0') {
        INNER_LOG(CLOG_WARNING, "Cannot get self ip address, using default ip hex: %s", g_config.defaultIpHex);
        g_cat_messageManager.ipHex = catsdscpy(g_cat_messageManager.ipHex, g_config.defaultIpHex);
    }
    INNER_LOG(CLOG_INFO, "Current ip hex: %s", g_cat_messageManager.ipHex);
}

void catMessageManagerDestroy() {
    catsdsfree(g_cat_messageManager.domain);
    g_cat_messageManager.domain = NULL;
    catsdsfree(g_cat_messageManager.hostname);
    g_cat_messageManager.hostname = NULL;
    catsdsfree(g_cat_messageManager.ip);
    g_cat_messageManager.ip = NULL;
    catsdsfree(g_cat_messageManager.ipHex);
    g_cat_messageManager.ipHex = NULL;
}

void catMessageManagerStartTrans(CatTransaction *trans) {
    CatContext *ctx = getCatContext();
    ctx->startTrans(ctx, trans);
}
