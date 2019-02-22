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
#include "aggregator_metric.h"

#include "client_config.h"
#include "functions.h"
#include "message.h"

#include <lib/cat_atomic.h>
#include <lib/cat_ccmap.h>

typedef struct _CatMetricData {
    ATOMICLONG m_count;
    ATOMICLONG m_durationMsSum;
    ATOMICLONG m_slowCount;
    int m_slowThreshold;
    int m_latestFlag;
} CatMetricData;

static CCHashMap *g_metricAggregator;

static inline CatMetricData *createCatMetricData() {
    CatMetricData *pData = (CatMetricData *) malloc(sizeof(CatMetricData));
    catChecktPtr(pData);
    if (pData == NULL) {
        return NULL;
    }
    pData->m_count = 0;
    pData->m_durationMsSum = 0;
    pData->m_slowCount = 0;
    pData->m_slowThreshold = 0;
    pData->m_latestFlag = 0;
    return pData;
}

static inline void destroyCatMetricData(CatMetricData *pData) {
    if (pData == NULL) {
        return;
    }
    free(pData);
}

static inline void addCountMetricToData(CatMetricData *pData, int value) {
    ATOMICLONG_ADD(&pData->m_count, value);
}


static inline void addTimerMetricToData(CatMetricData *pData, int timeMs) {
    ATOMICLONG_INC(&pData->m_count);
    ATOMICLONG_ADD(&pData->m_durationMsSum, timeMs);

    if (pData->m_slowThreshold > 0 && timeMs > pData->m_slowThreshold) {
        ATOMICLONG_INC(&pData->m_slowCount);
    }
}


static inline void addLatestMetricToData(CatMetricData *pData, int quantity) {
    pData->m_count = quantity;
    pData->m_latestFlag = 1;
}


static void newAggregatorMetric(char *name, char *status, char *keyValuePairs) {
    CatMetric *metric = newMetric("", name);
    catChecktPtr(metric);
    if (metric == NULL) {
        return;
    }
    if (keyValuePairs != NULL) {
        metric->addData(metric, keyValuePairs);
    }
    metric->setStatus(metric, status);
    metric->complete(metric);
}

static void MetricDataValOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    char tmpBuf[32];
    CatMetricData *pData = (CatMetricData *) (*ppVal);

    char *keyName = (char *) key;

    if (pData->m_durationMsSum > 0) {
        char keyValPair[32];
        strcpy(keyValPair, catItoA(pData->m_count, tmpBuf, 10));
        strcat(keyValPair, ",");
        strcat(keyValPair, catItoA(pData->m_durationMsSum, tmpBuf, 10));
        newAggregatorMetric(keyName, "S,C", keyValPair);
    } else if (pData->m_count > 0) {
        if (pData->m_latestFlag) {
            newAggregatorMetric(keyName, "L", catItoA(pData->m_count, tmpBuf, 10));
        } else {
            newAggregatorMetric(keyName, "C", catItoA(pData->m_count, tmpBuf, 10));
        }
    }

    if (pData->m_slowCount > 0) {
        sds newName = catsdsnew(keyName);
        newName = catsdscat(newName, ".slowCount");
        newAggregatorMetric(newName, "C", catItoA(pData->m_slowCount, tmpBuf, 10));
        catsdsfree(newName);
    }

    pData->m_count = 0;
    pData->m_durationMsSum = 0;
    pData->m_slowCount = 0;
    pData->m_latestFlag = 0;
}

static void sendMetricDataNoClear() {
    optEveryCCHashMapItem(g_metricAggregator, MetricDataValOptFun, NULL);
}

static void findValCountOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    volatile void *pVal = *ppVal;
    CatMetricData *pData = (CatMetricData *) pVal;
    addCountMetricToData(pData, (int) pParam);
}

static void findValTimerOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    volatile void *pVal = *ppVal;
    CatMetricData *pData = (CatMetricData *) pVal;
    addTimerMetricToData(pData, (int) pParam);
}

static void findValLatestOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    volatile void *pVal = *ppVal;
    CatMetricData *pData = (CatMetricData *) pVal;
    addLatestMetricToData(pData, (int) pParam);
}


static void findValThresholdOptFun(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam) {
    volatile void *pVal = *ppVal;
    CatMetricData *pData = (CatMetricData *) pVal;
    pData->m_slowThreshold = (int) pParam;
}


static void *createValFun(CCHashMap *pCCHM, void *key, void *pParam) {
    return createCatMetricData();
}

void addCountMetricToAggregator(const char *name, int count) {
    findCCHashMapCreateByFunAndOperate(g_metricAggregator, (char *) name, createValFun,
                                       NULL, findValCountOptFun, (void *) count);
}

void addDurationMetricToAggregator(const char *name, int timeMs) {
    findCCHashMapCreateByFunAndOperate(g_metricAggregator, (char *) name, createValFun,
                                       NULL, findValTimerOptFun, (void *) timeMs);
}

void sendMetricData() {
    if (g_metricAggregator->m_count <= 0) {
        return;
    }
    getContextMessageTree()->canDiscard = 0;
    CatTransaction *pTrans = newTransaction("System", "MetricAggregator");
    sendMetricDataNoClear();

    pTrans->setStatus(pTrans, CAT_SUCCESS);
    pTrans->complete(pTrans);
}

extern unsigned int _dictStringCopyHTHashFunction(const void *key);

extern void *_dictStringCopyHTKeyDup(void *privdata, const void *key);

extern void *_dictStringKeyValCopyHTValDup(void *privdata, const void *val);

extern int _dictStringCopyHTKeyCompare(void *privdata, const void *key1,
                                       const void *key2);

extern void _dictStringCopyHTKeyDestructor(void *privdata, void *key);


static void catMetricDataFreeFun(void *privdata, void *val) {
    destroyCatMetricData((CatMetricData *) val);
}

dictType dictTypeCatMetricAggregator = {
        _dictStringCopyHTHashFunction,        /* hash function */
        _dictStringCopyHTKeyDup,              /* key dup */
        NULL,                               /* val dup */
        _dictStringCopyHTKeyCompare,          /* key compare */
        _dictStringCopyHTKeyDestructor,       /* key destructor */
        catMetricDataFreeFun  /* val destructor */
};

void initCatMetricAggregator() {
    g_metricAggregator = createCCHashMap(&dictTypeCatMetricAggregator, 16, NULL);
    catChecktPtr(g_metricAggregator);
}

void destroyCatMetricAggregator() {
    destroyCCHashMap(g_metricAggregator);
}
