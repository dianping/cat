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
#include "cat_static_queue.h"

CATStaticQueue *createCATStaticQueue(size_t maxQueueSize) {
    CATStaticQueue *pQue = (CATStaticQueue *) malloc(sizeof(CATStaticQueue) + maxQueueSize * sizeof(void *));
    if (pQue == NULL) {
        return NULL;
    }
    pQue->head = 0;
    pQue->tail = 0;
    pQue->size = 0;
    pQue->maxQueueSize = maxQueueSize;
    return pQue;
}

int pushBackCATStaticQueue(CATStaticQueue *pQueue, void *pData) {
    if (isCATStaticQueueFull(pQueue)) {
        return CATSTATICQUEUE_ERR;
    }

    if (isCATStaticQueueEmpty(pQueue)) {
        pQueue->tail = 0;
        pQueue->head = 0;
        pQueue->size = 1;
        pQueue->valueArray[0] = pData;
        return CATSTATICQUEUE_OK;
    }

    if (++pQueue->head == pQueue->maxQueueSize) {
        pQueue->head = 0;
    }
    pQueue->valueArray[pQueue->head] = pData;
    ++pQueue->size;

    return CATSTATICQUEUE_OK;
}

int pushFrontCATStaticQueue(CATStaticQueue *pQueue, void *pData) {
    if (isCATStaticQueueFull(pQueue)) {
        return CATSTATICQUEUE_ERR;
    }

    if (isCATStaticQueueEmpty(pQueue)) {
        pQueue->tail = 0;
        pQueue->head = 0;
        pQueue->size = 1;
        pQueue->valueArray[0] = pData;
        return CATSTATICQUEUE_OK;
    }

    if (--pQueue->tail < 0) {
        pQueue->tail = pQueue->maxQueueSize - 1;
    }
    pQueue->valueArray[pQueue->tail] = pData;

    ++pQueue->size;
    return CATSTATICQUEUE_OK;
}

void *popBackCATStaticQueue(CATStaticQueue *pQueue) {
    if (isCATStaticQueueEmpty(pQueue)) {
        return NULL;
    }

    void *pData = pQueue->valueArray[pQueue->head];

    if (--pQueue->size == 0) {
        pQueue->tail = 0;
        pQueue->head = 0;
        pQueue->size = 0;
        return pData;
    } else {
        if (--pQueue->head < 0) {
            pQueue->head = pQueue->maxQueueSize - 1;
        }
    }

    return pData;
}

void *popFrontCATStaticQueue(CATStaticQueue *pQueue) {
    if (isCATStaticQueueEmpty(pQueue)) {
        return NULL;
    }
    void *pData = pQueue->valueArray[pQueue->tail];

    if (--pQueue->size == 0) {
        pQueue->tail = 0;
        pQueue->head = 0;
        pQueue->size = 0;
        return pData;
    } else {
        if (++pQueue->tail == pQueue->maxQueueSize) {
            pQueue->tail = 0;
        }
    }

    return pData;
}

void *pryBackCATStaticQueue(CATStaticQueue *pQueue) {
    if (isCATStaticQueueEmpty(pQueue)) {
        return NULL;
    }

    return pQueue->valueArray[pQueue->head];
}

void *pryFrontCATStaticQueue(CATStaticQueue *pQueue) {
    if (isCATStaticQueueEmpty(pQueue)) {
        return NULL;
    }

    return pQueue->valueArray[pQueue->tail];
}

void *getCATStaticQueueByIndex(CATStaticQueue *pQueue, size_t index) {
    if (!pQueue->size || pQueue->size - 1 < index) {
        return NULL;
    }
    index += pQueue->tail;
    if (index >= pQueue->maxQueueSize) {
        index -= pQueue->maxQueueSize;
    }
    return pQueue->valueArray[index];
}

void clearCATStaticQueue(CATStaticQueue *pQueue) {
    pQueue->head = 0;
    pQueue->size = 0;
    pQueue->tail = 0;
}

void destroyCATStaticQueue(CATStaticQueue *pQueue) {
    free(pQueue);
}
