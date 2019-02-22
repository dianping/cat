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
#include "aggregator_event.h"

#include "message.h"
#include "client_config.h"
#include "functions.h"

#include <lib/cat_atomic.h>
#include <lib/cat_ccmap.h>
#include <lib/cat_thread.h>

typedef struct _CatEventData {
    sds m_type;
    sds m_name;
    ATOMICLONG m_count;
    ATOMICLONG m_error;
} CatEventData;

static CCHashMap *g_eventAggregator;

static inline sds buildKey(CatMessage *pMsg) {
    static CATTHREADLOCAL sds s_key = NULL;
    if (s_key == NULL) {
        s_key = catsdsnewEmpty(128);
    }

    CatMessageInner *pInnerMsg = getInnerMsg(pMsg);
    sds type = pInnerMsg->type;
    sds name = pInnerMsg->name;
    s_key = catsdscpylen(s_key, type, catsdslen(type));
    s_key = catsdscatchar(s_key, ',');
    s_key = catsdscatlen(s_key, name, catsdslen(name));
    return s_key;
}

static inline CatEventData *createCatEventData(CatEvent *pEvent) {
    CatMessageInner *pInnerMsg = getInnerMsg(pEvent);
    sds type = pInnerMsg->type;
    sds name = pInnerMsg->name;
    CatEventData *pData = (CatEventData *) malloc(sizeof(CatEventData));
    catChecktPtr(pData);
    pData->m_type = catsdsdup(type);
    pData->m_name = catsdsdup(name);
    pData->m_count = 0;
    pData->m_error = 0;
    return pData;
}

static inline void destroyCatEventData(CatEventData *pData) {
    if (pData == NULL) {
        return;
    }
    catsdsfree(pData->m_type);
    catsdsfree(pData->m_name);
    free(pData);
}

static inline void addEventToData(CatEventData *pData, CatEvent *pEvent) {
    ATOMICLONG_INC(&pData->m_count);
    if (!checkCatMessageSuccess(pEvent)) {
        ATOMICLONG_INC(&pData->m_error);
    }
}

static inline void addBatchEventToData(CatEventData *pData, int count, int error) {
    ATOMICLONG_ADD(&pData->m_count, (long) count);
    ATOMICLONG_ADD(&pData->m_error, (long) error);
}

static void eventDataValOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    char tmpBuf[32];
    CatEventData *pData = (CatEventData *) (*ppVal);
    if (pData->m_count > 0) {
        CatEvent *pEvent = newEvent(pData->m_type, pData->m_name);
        CatMessageInner *pInner = getInnerMsg(pEvent);
        pInner->data = catsdsnewEmpty(64);
        pInner->data = catsdscatchar(pInner->data, '@');
        pInner->data = catsdscat(pInner->data, catItoA(pData->m_count, tmpBuf, 10));
        pInner->data = catsdscatchar(pInner->data, ';');
        pInner->data = catsdscat(pInner->data, catItoA(pData->m_error, tmpBuf, 10));
        pEvent->setStatus(pEvent, CAT_SUCCESS);
        pEvent->complete(pEvent);
    }
    pData->m_count = 0;
    pData->m_error = 0;
}

static void sendEventDataNoClear() {
    optEveryCCHashMapItem(g_eventAggregator, eventDataValOptFun, NULL);
}

static void sendEventDataClear() {
    dict **pDictArray = moveCCHashMap(g_eventAggregator);
    dict **pHeadDictArray = pDictArray;
    catChecktPtr(pDictArray);
    if (pDictArray == NULL) {
        return;
    }
    dict *pDict = NULL;
    while ((pDict = *pDictArray++) != NULL) {
        dictIterator *iter = catDictGetIterator(pDict);
        if (iter != NULL) {
            dictEntry *pEntry = NULL;
            while ((pEntry = catDictNext(iter)) != NULL) {
                eventDataValOptFun(NULL, pEntry->key, &pEntry->val, NULL);
            }
            catDictReleaseIterator(iter);
        }
        catDictRelease(pDict);
    }
    freeDictArray(pHeadDictArray);
}

static void findValOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    CatEvent *pEvent = (CatEvent *) pParam;
    volatile void *pVal = *ppVal;
    CatEventData *pData = (CatEventData *) pVal;
    addEventToData(pData, pEvent);
}

static void findValOptBatchFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    void **params = (void **) pParam;

    int *count = params[0];
    int *error = params[1];

    volatile void *pVal = *ppVal;
    CatEventData *pData = (CatEventData *) pVal;
    addBatchEventToData(pData, *count, *error);
}


static void *createValFun(CCHashMap *pCCHM, void *key, void *pParam) {
    CatEvent *pEvent = (CatEvent *) pParam;
    return createCatEventData(pEvent);
}

void addEventToAggregator(CatEvent *pEvent) {
    sds key = buildKey(pEvent);
    findCCHashMapCreateByFunAndOperate(g_eventAggregator, key, createValFun, pEvent, findValOptFun, pEvent);
}

void sendEventData() {
    if (g_eventAggregator->m_count <= 0) {
        return;
    }
    getContextMessageTree()->canDiscard = 0;
    CatTransaction *pTrans = newTransaction("System", "EventAggregator");
    if (g_eventAggregator->m_count < 2000) {
        sendEventDataNoClear();
    } else {
        sendEventDataClear();
    }

    pTrans->setStatus(pTrans, CAT_SUCCESS);
    pTrans->complete(pTrans);
}

extern unsigned int _dictStringCopyHTHashFunction(const void *key);

extern void *_dictStringCopyHTKeyDup(void *privdata, const void *key);

extern void *_dictStringKeyValCopyHTValDup(void *privdata, const void *val);

extern int _dictStringCopyHTKeyCompare(void *privdata, const void *key1,
                                       const void *key2);

extern void _dictStringCopyHTKeyDestructor(void *privdata, void *key);


static void catEventDataFreeFun(void *privdata, void *val) {
    destroyCatEventData((CatEventData *) val);
}


dictType dictTypeCatEventAggregator = {
        _dictStringCopyHTHashFunction,        /* hash function */
        _dictStringCopyHTKeyDup,              /* key dup */
        NULL,                               /* val dup */
        _dictStringCopyHTKeyCompare,          /* key compare */
        _dictStringCopyHTKeyDestructor,       /* key destructor */
        catEventDataFreeFun  /* val destructor */
};

void initCatEventAggregator() {
    g_eventAggregator = createCCHashMap(&dictTypeCatEventAggregator, 16, NULL);
    catChecktPtr(g_eventAggregator);
}

void destroyCatEventAggregator() {
    destroyCCHashMap(g_eventAggregator);
}
