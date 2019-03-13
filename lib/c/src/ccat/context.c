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
#include "context.h"

#include "client_config.h"
#include "message.h"
#include "message_manager.h"
#include "functions.h"

#include <lib/cat_thread.h>
#include <lib/cat_time_util.h>

extern CatMessageManager g_cat_messageManager;

CATTHREADLOCAL CatContext *g_cat_context = NULL;

void catContextAddMessage(CatContext *ctx, CatMessage *message) {
    CATStaticStack *pStack = ctx->transactionStack;
    if (isCATStaticStackEmpty(pStack)) {
        CatMessageTree *pRootCopy = copyCatMessageTree(ctx->tree);
        pRootCopy->root = message;
        catMessageManagerFlush(pRootCopy);
    } else {
        CatTransaction *parent = peekCATStaticStack(pStack);
        ctx->addTransChild(ctx, message, parent);
    }
}

void catContextAddTransChild(CatContext *ctx, CatMessage *message, CatTransaction *trans) {
    unsigned long long treePeriod = catTrimToHour(getCatMessageTimeStamp(ctx->tree->root));
    // 10 seconds extra time allowed
    unsigned long long messagePeriod = catTrimToHour(getCatMessageTimeStamp(message) - 10 * 1000L);

    if (treePeriod < messagePeriod || ctx->elementSize >= g_config.maxContextElementSize) {
        truncateAndFlush(ctx, getCatMessageTimeStamp(message));
    }

    trans->addChild(trans, message);
    ++ctx->elementSize;
}

void catContextAdjustForTruncatedTrans(CatContext *ctx, CatTransaction *root) {
    CatEvent *next = createCatEvent("TruncatedTransaction", "TotalDuration");
    unsigned long long actualDurationUs = ctx->lastTruncateTransDurationUs + getCatTransactionDurationUs(root);

    next->addData(next, catsdsfromlonglong(actualDurationUs));
    next->setStatus(next, CAT_SUCCESS);
    root->addChild(root, next);

    ctx->lastTruncateTransDurationUs = 0;
}

void catContextStartTrans(CatContext *ctx, CatTransaction *trans) {
    if (!isCATStaticStackEmpty(ctx->transactionStack)) {
        CatTransaction *parent = peekCATStaticStack(ctx->transactionStack);
        catContextAddTransChild(ctx, (CatMessage *) trans, parent);
    } else {
        CatMessageTree *t = ctx->tree;
        ctx->tree->root = (CatMessage *) trans;
    }

    pushCATStaticStack(ctx->transactionStack, trans);
}

void catContextEndTrans(CatContext *ctx, CatTransaction *trans) {
    CatTransaction *stackTrans;
    while (getCATStaticStackSize(ctx->transactionStack) > 0) {
        stackTrans = getCATStaticStackByIndex(ctx->transactionStack, 0);
        popFrontCATStaticQueue(ctx->transactionStack);
        if (stackTrans == trans) {
            break;
        }
    }
}

void catContextReset(CatContext *ctx) {
    if (NULL != ctx->tree->messageId) {
        catsdsfree(ctx->tree->messageId);
        ctx->tree->messageId = NULL;
    }

    if (NULL != ctx->tree->parentMessageId) {
        catsdsfree(ctx->tree->parentMessageId);
        ctx->tree->parentMessageId = NULL;
    }

    if (NULL != ctx->tree->rootMessageId) {
        catsdsfree(ctx->tree->rootMessageId);
        ctx->tree->rootMessageId = NULL;
    }

    ctx->tree->root = NULL;

    ctx->elementSize = 0;
    ctx->lastTruncateTransDurationUs = 0;
    clearCATStaticStack(ctx->transactionStack);
}

CatContext *newCatContext() {
    CatContext *ctx = (CatContext *) malloc(sizeof(CatContext));
    catChecktPtr(ctx);
    if (ctx == NULL) {
        return NULL;
    }

    ctx->lastTruncateTransDurationUs = 0;
    ctx->elementSize = 0;

    ctx->tree = createCatMessageTree();
    catChecktPtr(ctx->tree);

    ctx->transactionStack = createCATStaticStack(g_config.maxContextElementSize);
    catChecktPtr(ctx->transactionStack);

    if (ctx->tree != NULL) {
        ctx->tree->root = NULL;

        int pid = cat_get_current_thread_id();
        char tmpBuf[32];
        ctx->tree->threadId = catsdsnew(catItoA(pid, tmpBuf, 10));
        ctx->tree->threadGroupName = catsdsnew("cat");
        ctx->tree->threadName = catsdsnew("cat");
        // catChecktPtr(ctx->tree->threadName);
    }

    ctx->addMessage = catContextAddMessage;
    ctx->addTransChild = catContextAddTransChild;
    ctx->adjustForTruncatedTrans = catContextAdjustForTruncatedTrans;
    ctx->startTrans = catContextStartTrans;
    ctx->endTrans = catContextEndTrans;
    ctx->reset = catContextReset;
    return ctx;
}

CatContext *getCatContext() {
    if (NULL == g_cat_context) {
        g_cat_context = newCatContext();
    }
    return g_cat_context;
}

CatMessageTree *getContextMessageTree() {
    return getCatContext()->tree;
}
