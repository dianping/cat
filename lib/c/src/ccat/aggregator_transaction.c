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
#include "aggregator_transaction.h"

#include "client_config.h"
#include "functions.h"
#include "message.h"

#include <lib/cat_atomic.h>
#include <lib/cat_ccmap.h>
#include <lib/cat_clog.h>
#include <lib/cat_thread.h>

typedef struct _CatTransData {
    sds m_type;
    sds m_name;
    ATOMICLONG m_count;
    ATOMICLONG m_durationMsSum;
    ATOMICLONG m_error;
    CCHashMap *m_durations;
} CatTransData;

static CCHashMap *g_transAggregator;

extern unsigned int _dictStringCopyHTHashFunction(const void *key);

extern void *_dictStringCopyHTKeyDup(void *privdata, const void *key);

extern void *_dictStringKeyValCopyHTValDup(void *privdata, const void *val);

extern int _dictStringCopyHTKeyCompare(void *privdata, const void *key1,
                                       const void *key2);

extern void _dictStringCopyHTKeyDestructor(void *privdata, void *key);

static void catTransDurationFreeFun(void *privdata, void *val) {
    free((ATOMICLONG *) val);
}

dictType dictCatTransDurations = {
        _dictStringCopyHTHashFunction,        /* hash function */
        _dictStringCopyHTKeyDup,              /* key dup */
        NULL,                               /* val dup */
        _dictStringCopyHTKeyCompare,          /* key compare */
        _dictStringCopyHTKeyDestructor,       /* key destructor */
        catTransDurationFreeFun  /* val destructor */
};

static inline long computeDuration(long duration) {
    if (duration < 1) {
        return 1;
    } else if (duration < 20) {
        return duration;
    } else if (duration < 200) {
        return duration - duration % 5;
    } else if (duration < 500) {
        return duration - duration % 20;
    } else if (duration < 2000) {
        return duration - duration % 50;
    } else {
        return duration - duration % 200;
    }
}

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

static inline sds buildDurationString(CatTransData *pData) {
    sds durationStr = catsdsnewEmpty(128);
    CCHashMap *durations = pData->m_durations;
    int first = 1;
    char tmpBuf[32];
    int i = 0;

    for (i = 0; i < durations->m_hashSlotCount; ++i) {
        CATCS_ENTER(durations->m_hashSlot[i].m_lock);
        if (durations->m_hashSlot[i].m_dict != NULL) {
            dictIterator *iter = catDictGetIterator(durations->m_hashSlot[i].m_dict);
            if (iter != NULL) {
                dictEntry *pEntry = NULL;
                while ((pEntry = catDictNext(iter)) != NULL) {
                    if (first) {
                        durationStr = catsdscat(durationStr, pEntry->key);
                        durationStr = catsdscatchar(durationStr, ',');
                        durationStr = catsdscat(durationStr, catItoA((int) (*(ATOMICLONG * )(pEntry->val)), tmpBuf, 10));
                        first = 0;
                    } else {
                        durationStr = catsdscatchar(durationStr, '|');
                        durationStr = catsdscat(durationStr, pEntry->key);
                        durationStr = catsdscatchar(durationStr, ',');
                        durationStr = catsdscat(durationStr, catItoA((int) (*(ATOMICLONG * )(pEntry->val)), tmpBuf, 10));
                    }
                }
                catDictReleaseIterator(iter);
            }
        }
        CATCS_LEAVE(durations->m_hashSlot[i].m_lock);
    }
    return durationStr;
}

static inline CatTransData *createCatTransData(CatTransaction *pTrans) {
    CatMessageInner *pInnerMsg = getInnerMsg(pTrans);
    sds type = pInnerMsg->type;
    sds name = pInnerMsg->name;
    CatTransData *pData = (CatTransData *) malloc(sizeof(CatTransData));
    catChecktPtr(pData);
    if (pData == NULL) {
        return NULL;
    }
    pData->m_type = catsdsdup(type);
    pData->m_name = catsdsdup(name);
    pData->m_count = 0;
    pData->m_error = 0;
    pData->m_durationMsSum = 0;
    pData->m_durations = createCCHashMap(&dictCatTransDurations, 16, NULL);
    return pData;
}

static inline void destroyCatTransData(CatTransData *pData) {
    if (pData == NULL) {
        return;
    }
    catsdsfree(pData->m_type);
    catsdsfree(pData->m_name);
    destroyCCHashMap(pData->m_durations);
    free(pData);
}

static void *createDurationFun(CCHashMap *pCCHM, void *key, void *pParam) {
    ATOMICLONG *count = (ATOMICLONG *) malloc(sizeof(ATOMICLONG));
    catChecktPtr(count);
    memset(count, 0, sizeof(ATOMICLONG));
    return count;
}

static inline void addTransToData(CatTransData *pData, CatTransaction *pTrans) {
    ATOMICLONG_INC(&pData->m_count);
    if (!checkCatMessageSuccess((CatMessage *) pTrans)) {
        ATOMICLONG_INC(&pData->m_error);
    }

    long rawDuration = (long) (getCatTransactionDurationUs(pTrans) / 1000);
    ATOMICLONG_ADD(&pData->m_durationMsSum, rawDuration);

    long duration = computeDuration(rawDuration);
    char tmpBuf[32];
    sds key = catsdsnewEmpty(128);
    key = catsdscat(key, catItoA(duration, tmpBuf, 10));

    ATOMICLONG *count = (ATOMICLONG *) findCCHashMapCreateByFun(pData->m_durations, key, createDurationFun, NULL);
    ATOMICLONG_INC(count);

    catsdsfree(key);
}

static inline void addBatchTransToData(CatTransData *pData, int count, int error, unsigned long long sum) {
    ATOMICLONG_ADD(&pData->m_count, (long) count);
    ATOMICLONG_ADD(&pData->m_error, (long) error);
    ATOMICLONG_ADD(&pData->m_durationMsSum, (long) sum);

    if (count == 1) {
        long duration = computeDuration((long) (sum));
        char tmpBuf[32];
        sds key = catsdsnewEmpty(128);
        key = catsdscat(key, catItoA(duration, tmpBuf, 10));

        ATOMICLONG *count = (ATOMICLONG *) findCCHashMapCreateByFun(pData->m_durations, key, createDurationFun, NULL);
        ATOMICLONG_INC(count);

        catsdsfree(key);
    }
}

static void TransDataValOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    char tmpBuf[32];
    CatTransData *pData = (CatTransData *) (*ppVal);
    if (pData->m_count > 0) {
        CatTransaction *pTrans = newTransaction(pData->m_type, pData->m_name);
        CatMessageInner *pInner = getInnerMsg(pTrans);
        pInner->data = catsdsnewEmpty(128);
        pInner->data = catsdscatchar(pInner->data, '@');
        pInner->data = catsdscat(pInner->data, catItoA(pData->m_count, tmpBuf, 10));
        pInner->data = catsdscatchar(pInner->data, ';');
        pInner->data = catsdscat(pInner->data, catItoA(pData->m_error, tmpBuf, 10));
        pInner->data = catsdscatchar(pInner->data, ';');
        pInner->data = catsdscat(pInner->data, catItoA(pData->m_durationMsSum, tmpBuf, 10));
        pInner->data = catsdscatchar(pInner->data, ';');

        sds durationString = buildDurationString(pData);
        pInner->data = catsdscatsds(pInner->data, durationString);
        catsdsfree(durationString);

        pTrans->setStatus(pTrans, CAT_SUCCESS);
        pTrans->complete(pTrans);

        _CLog_debugInfo("%s | %s : %s\n", pInner->type, pInner->name, pInner->data);
    }

    pData->m_count = 0;
    pData->m_error = 0;
    pData->m_durationMsSum = 0;
    clearCCHashMap(pData->m_durations);
}

static void sendTransDataNoClear() {
    optEveryCCHashMapItem(g_transAggregator, TransDataValOptFun, NULL);
}

static void sendTransDataClear() {
    dict **pDictArray = moveCCHashMap(g_transAggregator);
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
                TransDataValOptFun(NULL, pEntry->key, &pEntry->val, NULL);
            }
            catDictReleaseIterator(iter);
        }
        catDictRelease(pDict);
    }
    freeDictArray(pHeadDictArray);
}

static void findValOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    CatTransaction *pTrans = (CatTransaction *) pParam;
    volatile void *pVal = *ppVal;
    CatTransData *pData = (CatTransData *) pVal;
    addTransToData(pData, pTrans);
}

static void findValOptBatchFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    void** params = (void**) pParam;

    int *count = params[0];
    int *error = params[1];
    unsigned long long *sum = params[2];

    volatile void *pVal = *ppVal;
    CatTransData *pData = (CatTransData *) pVal;
    addBatchTransToData(pData, *count, *error, *sum);
}


static void *createValFun(CCHashMap *pCCHM, void *key, void *pParam) {
    CatTransaction *pTrans = (CatTransaction *) pParam;
    return createCatTransData(pTrans);
}

void addTransToAggregator(CatTransaction *pTrans) {
    sds key = buildKey((CatMessage *) pTrans);
    findCCHashMapCreateByFunAndOperate(g_transAggregator, key, createValFun, pTrans, findValOptFun, pTrans);
}

void sendTransData() {
    if (g_transAggregator->m_count <= 0) {
        return;
    }
    getContextMessageTree()->canDiscard = 0;
    CatTransaction *pTrans = newTransaction("System", "TransactionAggregator");
    if (g_transAggregator->m_count < 2000) {
        sendTransDataNoClear();
    } else {
        sendTransDataClear();
    }

    pTrans->setStatus(pTrans, CAT_SUCCESS);
    pTrans->complete(pTrans);
}

static void catTransDataFreeFun(void *privdata, void *val) {
    destroyCatTransData((CatTransData *) val);
}

dictType dictTypeCatTransAggregator = {
        _dictStringCopyHTHashFunction,          /* hash function */
        _dictStringCopyHTKeyDup,                /* key dup */
        NULL,                                   /* val dup */
        _dictStringCopyHTKeyCompare,            /* key compare */
        _dictStringCopyHTKeyDestructor,         /* key destructor */
        catTransDataFreeFun                     /* val destructor */
};

void initCatTransAggregator() {
    g_transAggregator = createCCHashMap(&dictTypeCatTransAggregator, 16, NULL);
    catChecktPtr(g_transAggregator);
}

void destroyCatTransAggregator() {
    destroyCCHashMap(g_transAggregator);
}

