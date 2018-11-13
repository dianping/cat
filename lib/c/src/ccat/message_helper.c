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
#include "message_helper.h"

#include "ccat/message.h"
#include "ccat/message_id.h"
#include "ccat/message_manager.h"

#include "lib/cat_time_util.h"

static void migrateMessage(CATStaticStack *pStack, CatTransaction *source, CatTransaction *target, size_t level) {
    CatTransaction *current = level < getCATStaticStackSize(pStack) ?
                              (CatTransaction *) getCATStaticStackByIndex(pStack,
                                                                          getCATStaticStackSize(pStack) - level - 1)
                                                                    : NULL;
    int shouldKeep = 0;

    CATStaticQueue *children = getCatTransactionChildren(source);
    size_t i = 0;
    for (i = 0; i < getCATStaticQueueSize(children); ++i) {
        CatMessage *pMsg = getCATStaticQueueByIndex(children, i);
        if (pMsg != (CatMessage *) current) {
            target->addChild(target, pMsg);
        } else {
            CatTransaction *clonedTrans = copyCatTransaction(current);
            clonedTrans->setStatus(clonedTrans, CAT_SUCCESS);

            target->addChild(target, (CatMessage *) clonedTrans);
            migrateMessage(pStack, current, clonedTrans, level + 1);
            shouldKeep = 1;
        }
    }

    clearCATStaticQueue(children);

    if (shouldKeep) {
        pushBackCATStaticQueue(children, current);
    }
}

void truncateAndFlush(CatContext *context, unsigned long long timestampMs) {
    CatMessageTree *pRootMsg = context->tree;
    CATStaticStack *pStack = context->transactionStack;
    CatMessage *message = pRootMsg->root;

    if (!isCatTransaction(message)) {
        return;
    }
    sds id = pRootMsg->messageId;

    if (id == NULL) {
        id = getNextMessageId();
        pRootMsg->messageId = id;
    }
    sds rootId = pRootMsg->rootMessageId;
    sds childId = getNextMessageId();

    CatTransaction *source = (CatTransaction *) message;

    CatTransaction *target = copyCatTransaction(source);
    target->setStatus(target, CAT_SUCCESS);

    migrateMessage(pStack, source, target, 1);

    int i;
    i = getCATStaticQueueSize(pStack) - 1;
    for (; i >= 0; --i) {
        CatTransaction *t = (CatTransaction *) getCATStaticQueueByIndex(pStack, i);
        CatTransactionInner *tInner = getInnerTrans(t);
        tInner->message.timestampMs = timestampMs;
        tInner->durationStart = GetTime64() * 1000 * 1000;
    }
    CatEvent *next = createCatEvent("RemoteCall", "Next");

    next->addData(next, childId);
    next->setStatus(next, CAT_SUCCESS);
    target->addChild(target, next);

    // tree is the parent, and m_tree is the child.
    CatMessageTree *pCp = copyCatMessageTree(pRootMsg);

    pCp->root = (CatMessage *) target;

    pRootMsg->messageId = childId;

    if (pRootMsg->parentMessageId != NULL) {
        catsdsfree(pRootMsg->parentMessageId);
    }

    pRootMsg->parentMessageId = id;
    pRootMsg->rootMessageId = (rootId != NULL ? rootId : catsdsdup(id));

    context->elementSize = getCATStaticStackSize(pStack);
    context->lastTruncateTransDurationUs =
            context->lastTruncateTransDurationUs + getCatTransactionDurationUs(target);

    catMessageManagerFlush(pCp);
}

void markAsNotCompleted(CatTransaction *pTrans) {
    CatTransactionInner *transInner = getInnerTrans(pTrans);
    transInner->message.isComplete = 1;
}

void validateTransaction(CatTransaction *pParentTrans, CatTransaction *pTrans) {
    CatTransactionInner *pTransInner = getInnerTrans(pTrans);
    CATStaticQueue *pChildren = pTransInner->children;
    size_t i = 0;
    for (; i < getCATStaticQueueSize(pChildren); ++i) {
        CatMessage *pMsg = getCATStaticQueueByIndex(pChildren, i);
        if (isCatTransaction(pMsg)) {
            validateTransaction(pTrans, (CatTransaction *) pMsg);
        }
    }
    if (!isCatMessageComplete((CatMessage *) pTrans)) {
        markAsNotCompleted(pTrans);
    }
}

int isCatTransaction(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->messageType.type == CatMessageType_Trans;
}

int isCatEvent(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->messageType.type == CatMessageType_Event;
}

int isCatMetric(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->messageType.type == CatMessageType_Metric;
}

int isCatHeartBeat(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    return pInner->messageType.type == CatMessageType_HeartBeat;
}

