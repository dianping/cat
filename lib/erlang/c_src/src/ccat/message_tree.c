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
#include "message_tree.h"

#include "ccat/message.h"

CatMessageTree *copyCatMessageTree(CatMessageTree *pRootMsg) {
    CatMessageTree *pCopyMsg = (CatMessageTree *) malloc(sizeof(CatMessageTree));
    if (pCopyMsg == NULL) {
        return pCopyMsg;
    }
    if (pRootMsg == NULL) {
        memset(pCopyMsg, 0, sizeof(CatMessageTree));
        // default : set canDiscard = 1.
        pCopyMsg->canDiscard = 1;
    } else {
        pCopyMsg->root = pRootMsg->root;
        pCopyMsg->messageId = catsdsdup(pRootMsg->messageId);
        pCopyMsg->parentMessageId = catsdsdup(pRootMsg->parentMessageId);
        pCopyMsg->rootMessageId = catsdsdup(pRootMsg->rootMessageId);
        pCopyMsg->sessionToken = catsdsdup(pRootMsg->sessionToken);
        pCopyMsg->threadGroupName = catsdsdup(pRootMsg->threadGroupName);
        pCopyMsg->threadId = catsdsdup(pRootMsg->threadId);
        pCopyMsg->threadName = catsdsdup(pRootMsg->threadName);
        pCopyMsg->canDiscard = pRootMsg->canDiscard;
    }
    return pCopyMsg;
}

void clearCatMessageTree(CatMessageTree *pRootMsg) {
    pRootMsg->root = NULL;
    if (pRootMsg->messageId != NULL) {
        catsdsfree(pRootMsg->messageId);
        pRootMsg->messageId = NULL;
    }
    if (pRootMsg->parentMessageId != NULL) {
        catsdsfree(pRootMsg->parentMessageId);
        pRootMsg->parentMessageId = NULL;
    }
    if (pRootMsg->rootMessageId != NULL) {
        catsdsfree(pRootMsg->rootMessageId);
        pRootMsg->rootMessageId = NULL;
    }
    if (pRootMsg->sessionToken != NULL) {
        catsdsfree(pRootMsg->sessionToken);
        pRootMsg->sessionToken = NULL;
    }
    if (pRootMsg->threadGroupName != NULL) {
        catsdsfree(pRootMsg->threadGroupName);
        pRootMsg->threadGroupName = NULL;
    }
    if (pRootMsg->threadId != NULL) {
        catsdsfree(pRootMsg->threadId);
        pRootMsg->threadId = NULL;
    }
    if (pRootMsg->threadName != NULL) {
        catsdsfree(pRootMsg->threadName);
        pRootMsg->threadName = NULL;
    }
}

void deleteCatMessageTree(CatMessageTree *pRootMsg) {
    if (pRootMsg->root != NULL) {
        deleteCatMessage(pRootMsg->root);
        pRootMsg->root = NULL;
    }

    clearCatMessageTree(pRootMsg);

    free(pRootMsg);
}

CatMessageTree *createCatMessageTree() {
    return copyCatMessageTree(NULL);
}

