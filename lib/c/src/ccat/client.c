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
#include "client.h"

#include <lib/cat_clog.h>
#include <lib/cat_time_util.h>
#include <ccat/version.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "client_config.h"
#include "context.h"
#include "message.h"
#include "aggregator.h"
#include "aggregator_metric.h"
#include "message_id.h"
#include "message_manager.h"
#include "message_sender.h"
#include "monitor.h"
#include "server_connection_manager.h"

#ifdef WIN32
#ifdef CAT_MEMLEAK_DETECT
#include "vld.h"
#endif
#elif defined(__linux)
#include <signal.h>
#endif

static volatile int g_cat_init = 0;
extern volatile int g_cat_enabled;

volatile sds g_multiprocessing_pid_str = NULL;

extern CatMessage g_cat_nullMsg;
extern CatTransaction g_cat_nullTrans;

// TODO I really don't want to do this but I have to.
extern int g_server_activeId;

CatClientConfig DEFAULT_CCAT_CONFIG = {
        CAT_ENCODER_BINARY,
        1,  // enable heartbeat
        1,  // enable sampling
        0,  // disable multiprocessing
        0,  // disable debug log
        0,  // disable auto initialize when forked
};

void resetCatContext() {
    CatContext* ctx = getCatContext();
    ctx->reset(ctx);
}

static void catClientInitInner() {
    g_cat_init = 1;

    initMessageIdHelper();

    initCatAggregator();
    initCatMonitor();
    initCatSender();

    resetCatContext();

    // TODO Avoid using it.
    g_server_activeId = -1;

    if (!updateCatServerConn()) {
        g_cat_init = 0;
        g_cat_enabled = 0;
        INNER_LOG(CLOG_ERROR,
                  "Failed to initialize cat: Error occurred while getting router config from remote server.");
        return;
    }
    g_cat_enabled = 1;

    startCatAggregatorThread();
    startCatMonitorThread();
    startCatSenderThread();
}

static void catClientInitInnerForked() {
    g_cat_init = 0;

    // Disable the heartbeat if the process is forked from another thread.
    g_config.enableHeartbeat = 0;
    INNER_LOG(CLOG_INFO, "Master process has been forked, heartbeat will be disabled.");

    if (g_config.enableAutoInitialize) {
        catClientInitInner();
        INNER_LOG(CLOG_INFO, "All cat threads has been reestablished automatically.");
    }
}

int catClientInit(const char* appkey) {
    return catClientInitWithConfig(appkey, &DEFAULT_CCAT_CONFIG);
}

int catClientInitWithConfig(const char *appkey, CatClientConfig* config) {
    if (g_cat_init) {
        return 0;
    }
    g_cat_init = 1;

    signal(SIGPIPE, SIG_IGN);

    initCatClientConfig(config);

    char *cathomevar;
    cathomevar = catHome();
    printf("Using CAT_HOME=%s\n", cathomevar);

    // char *clientXML = cathomevar + 'client.xml';
    char *clientXMLFileName = "client.xml";
    char *clientXML = (char *) malloc(strlen(cathomevar) + strlen(clientXMLFileName));
    sprintf(clientXML, "%s%s", cathomevar, clientXMLFileName);

    printf("Using client xml=%s\n", clientXML);

    if (loadCatClientConfig(clientXML) < 0) {
        g_cat_init = 0;
        g_cat_enabled = 0;
        INNER_LOG(CLOG_ERROR, "Failed to initialize cat: Error occurred while loading client config.");
        return 0;
    }
    g_config.appkey = catsdsnew(appkey);

    initMessageManager(appkey, g_config.selfHost);

    initCatServerConnManager();

    catClientInitInner();

    pthread_atfork(NULL, NULL, catClientInitInnerForked);

    INNER_LOG(CLOG_INFO, "Cat has been initialized with appkey: %s", appkey);

    return 1;
}

int catClientDestroy() {
    g_cat_enabled = 0;
    g_cat_init = 0;

    clearCatMonitor();
    catMessageManagerDestroy();
    destroyAggregator();
    clearCatSenderThread();
    clearCatServerConnManager();
    destroyMessageIdHelper();
    clearCatClientConfig();
    return 1;
}

const char* catVersion() {
    return CCAT_VERSION;
}

void logError(const char *msg, const char *errStr) {
    getContextMessageTree()->canDiscard = 0;
    logEvent("Exception", msg, CAT_ERROR, errStr);
}

void logEvent(const char *type, const char *name, const char *status, const char *data) {
    if (!isCatEnabled()) {
        return;
    }
    CatEvent *event = newEvent(type, name);
    catChecktPtr(event);
    if (event == NULL) {
        return;
    }
    if (data != NULL) {
        event->addData(event, data);
    }
    event->setStatus(event, status);
    event->complete(event);
}

void _logMetric(const char *name, const char *status, const char *value)
{
    CatMetric *metric = newMetric("", name);
    catChecktPtr(metric);

    if (value != NULL) {
        metric->addData(metric, value);
    }
    metric->setStatus(metric, status);
    metric->complete(metric);
}

void logMetricForCount(const char *name, int quantity) {
    if (!isCatEnabled()) {
        return;
    }

    if (g_config.enableSampling) {
        addCountMetricToAggregator(name, quantity);
        return;
    }

    if (quantity == 1) {
        _logMetric(name, "C", "1");
    } else {
        sds val = catsdsfromlonglong(quantity);
        catChecktPtr(val);
        _logMetric(name, "C", val);
        catsdsfree(val);
    }
}

void logMetricForDuration(const char *name, unsigned long long duration) {
    if (!isCatEnabled()) {
        return;
    }

    if (g_config.enableSampling) {
        addDurationMetricToAggregator(name, duration);
        return;
    }

    sds val = catsdsfromlonglong(duration);
    catChecktPtr(val);
    _logMetric(name, "T", val);
    catsdsfree(val);
}

CatEvent *newEvent(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullMsg;
    }
    CatEvent *event = createCatEvent(type, name);
    catChecktPtr(event);
    return event;
}

CatMetric *newMetric(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullMsg;
    }
    CatMetric *metric = createCatMetric(type, name);
    catChecktPtr(metric);
    return metric;
}

CatHeartBeat *newHeartBeat(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullMsg;
    }
    getContextMessageTree()->canDiscard = 0;

    CatHeartBeat *hb = createCatHeartBeat(type, name);
    catChecktPtr(hb);
    return hb;
}

CatTransaction *newTransaction(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullTrans;
    }
    CatTransaction *trans = createCatTransaction(type, name);
    catChecktPtr(trans);
    if (trans == NULL) {
        return NULL;
    }
    catMessageManagerStartTrans(trans);
    return trans;
}

CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long duration) {
    CatTransaction* trans = newTransaction(type, name);
    trans->setDurationInMillis(trans, duration);
    if (duration < 60 * 1000) {
        trans->setTimestamp(trans, GetTime64() - duration);
    }
    return trans;
}

void newCompletedTransactionWithDuration(const char *type, const char *name, unsigned long long duration) {
    CatTransaction* trans = newTransactionWithDuration(type, name, duration);
    trans->complete(trans);
}

char *createMessageId() {
    if (!isCatEnabled()) {
        return NULL;
    }
    return getNextMessageId();
}

char *createRemoteServerMessageId(const char *appkey) {
    if (!isCatEnabled()) {
        return NULL;
    }
    return getNextMessageIdByAppkey(appkey);
}

char *getThreadLocalMessageTreeId() {
    if (!isCatEnabled()) {
        return NULL;
    }
    return getContextMessageTree()->messageId;
}

char *getThreadLocalMessageTreeRootId() {
    if (!isCatEnabled()) {
        return NULL;
    }
    return getContextMessageTree()->rootMessageId;
}

char *getThreadLocalMessageTreeParentId() {
    if (!isCatEnabled()) {
        return NULL;
    }
    return getContextMessageTree()->parentMessageId;
}

void setThreadLocalMessageTreeId(char *messageId) {
    if (!isCatEnabled()) {
        return;
    }
    CatMessageTree *pTree = getContextMessageTree();
    if (pTree->messageId != NULL) {
        catsdsfree(pTree->messageId);
        pTree->messageId = NULL;
    }
    pTree->messageId = catsdsnew(messageId);
}

void setThreadLocalMessageTreeRootId(char *messageId) {
    if (!isCatEnabled()) {
        return;
    }
    CatMessageTree *pTree = getContextMessageTree();
    if (pTree->rootMessageId!= NULL) {
        catsdsfree(pTree->rootMessageId);
        pTree->rootMessageId = NULL;
    }
    pTree->rootMessageId = catsdsnew(messageId);
}

void setThreadLocalMessageTreeParentId(char *messageId) {
    if (!isCatEnabled()) {
        return;
    }
    CatMessageTree *pTree = getContextMessageTree();
    if (pTree->parentMessageId!= NULL) {
        catsdsfree(pTree->parentMessageId);
        pTree->parentMessageId = NULL;
    }
    pTree->parentMessageId = catsdsnew(messageId);
}
