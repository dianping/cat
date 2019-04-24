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
#include "aggregator.h"

#include "message.h"
#include "aggregator_transaction.h"
#include "aggregator_event.h"
#include "aggregator_metric.h"
#include "client_config.h"

#include <lib/cat_thread.h>
#include <lib/cat_time_util.h>
#include <lib/cat_atomic.h>
#include <lib/cat_clog.h>

static volatile int g_cat_aggregatorStop = 0;
static pthread_t g_cat_aggregatorHandle = NULL;

static ATOMICLONG g_cat_hitCount = 0;
static volatile double g_sampleRate = 1.0;
static volatile unsigned long g_sendInterval = 1;

int hitSample() {
    if (!g_config.enableSampling) {
        return 1;
    }
    if (ATOMICLONG_INC(&g_cat_hitCount) % g_sendInterval == 0) {
        return 1;
    }
    return 0;
}

void setSampleRate(double sampleRate) {
    if (sampleRate > 1.0 || sampleRate < 0) {
        return;
    }
    INNER_LOG(CLOG_INFO, "cat client sample rate has been set to %lf", sampleRate);
    g_sampleRate = sampleRate;
    g_sendInterval = (unsigned long) (1 / sampleRate);
}

void analyzerProcessTransaction(CatTransaction *pTransaction) {
    addTransToAggregator(pTransaction);

    CATStaticQueue *pChildren = getCatTransactionChildren(pTransaction);
    if (pChildren == NULL) {
        return;
    }

    size_t len = getCATStaticStackSize(pChildren);
    size_t i = 0;
    for (; i < len; i++) {
        CatMessage *pChild = getCATStaticStackByIndex(pChildren, i);
        if (isCatTransaction(pChild)) {
            analyzerProcessTransaction((CatTransaction *) pChild);
        } else if (isCatEvent(pChild)) {
            addEventToAggregator(pChild);
        }
    }

}

void sendToAggregator(CatMessageTree *pMsgTree) {
    if (!g_config.enableSampling) {
        return;
    }
    if (isCatTransaction(pMsgTree->root)) {
        analyzerProcessTransaction((CatTransaction *) pMsgTree->root);
    } else if (isCatEvent(pMsgTree->root)) {
        addEventToAggregator(pMsgTree->root);
    }
}

PTHREAD catAggregatorDataUpdateFun(PVOID para) {
    cat_set_thread_name("cat-aggregator");

    while (!g_cat_aggregatorStop) {
        long long startTime = GetTime64();

        sendTransData();
        sendEventData();
        sendMetricData();
        // sendMetricTagData();

        long long duration = GetTime64() - startTime;

        if (duration < 1000) {
            Sleep(1000 - (int) duration);
        }
    }

    return 0;
}


void initCatAggregator() {
    initCatTransAggregator();
    initCatEventAggregator();
    initCatMetricAggregator();
    // initCatMetricTagAggregator();

    g_cat_aggregatorStop = 0;
}

void startCatAggregatorThread() {
    pthread_create(&g_cat_aggregatorHandle, NULL, catAggregatorDataUpdateFun, NULL);
}

void destroyAggregator() {
    g_cat_aggregatorStop = 1;
    pthread_join(g_cat_aggregatorHandle, NULL);

    destroyCatTransAggregator();
    destroyCatEventAggregator();
    destroyCatMetricAggregator();
    // destroyCatMetricTagAggregator();
}

