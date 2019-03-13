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
#include "ccat/message.h"
#include "ccat/message_manager.h"

#include "lib/cat_time_util.h"

static void addDataNull(CatMessage *message, const char *data) {}

static void addKVNull(CatMessage *message, const char *dataKey, const char *dataValue) {};

static void setStatusNull(CatMessage *message, const char *status) {}

static void setTimestampNull(CatMessage *message, unsigned long long timestamp) {}

static void setCompleteNull(CatMessage *message) {}

CatMessage g_cat_nullMsg = {
        addDataNull,
        addKVNull,
        setStatusNull,
        setTimestampNull,
        setCompleteNull
};

static void addData(CatMessage *message, const char *data) {
    CatMessageInner *pInner = getInnerMsg(message);
    if (NULL == pInner->data) {
        pInner->data = catsdsnew(data);
    } else {
        pInner->data = catsdscat(pInner->data, "&");
        pInner->data = catsdscat(pInner->data, data);
    }
}

static void addKV(CatMessage *message, const char *dataKey, const char *dataValue) {
    CatMessageInner *pInner = getInnerMsg(message);
    if (NULL == pInner->data) {
        pInner->data = catsdsnew(dataKey);
        pInner->data = catsdscat(pInner->data, "=");
        pInner->data = catsdscat(pInner->data, dataValue);
    } else {
        pInner->data = catsdscat(pInner->data, "&");
        pInner->data = catsdscat(pInner->data, dataKey);
        pInner->data = catsdscat(pInner->data, "=");
        pInner->data = catsdscat(pInner->data, dataValue);
    }
}

static void setStatus(CatMessage *message, const char *status) {
    CatMessageInner *pInner = getInnerMsg(message);
    if (pInner->status == NULL) {
        pInner->status = catsdsnew(status);
    } else {
        pInner->status = catsdscpy(pInner->status, status);
    }
}

static void setComplete(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    pInner->isComplete = 1;
}

static void setTimestamp(CatMessage *message, unsigned long long timeMs) {
    CatMessageInner *pInner = getInnerMsg(message);
    pInner->timestampMs = timeMs;
}

void *clearMessage(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    if (pInner->status != NULL) {
        catsdsfree(pInner->status);
        pInner->status = NULL;
    }
    if (pInner->data != NULL) {
        catsdsfree(pInner->data);
        pInner->data = NULL;
    }
    if (pInner->type != NULL) {
        catsdsfree(pInner->type);
        pInner->type = NULL;
    }
    if (pInner->name != NULL) {
        catsdsfree(pInner->name);
        pInner->name = NULL;
    }
    return pInner;
}

void initCatMessage(CatMessage *pMsg, char msgType, const char *type, const char *name) {
    CatMessageInner *pInner = getInnerMsg(pMsg);
    memset(pInner, 0, sizeof(CatMessage) + sizeof(CatMessageInner));
    pInner->messageType.type = msgType;
    pInner->timestampMs = GetTime64();
    pInner->type = catsdsnew(type);
    pInner->name = catsdsnew(name);

    pMsg->addData = addData;
    pMsg->addKV = addKV;
    pMsg->complete = setComplete;
    pMsg->setTimestamp = setTimestamp;
    pMsg->setStatus = setStatus;
}

static void setEventComplete(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    pInner->isComplete = 1;
    catMessageManagerAdd(message);
}

static void setMetricComplete(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    pInner->isComplete = 1;
    catMessageManagerAdd(message);
}

static void setHeartBeatComplete(CatMessage *message) {
    CatMessageInner *pInner = getInnerMsg(message);
    pInner->isComplete = 1;
    catMessageManagerAdd(message);
}

CatEvent *createCatEvent(const char *type, const char *name) {
    CatMessageInner *pEventInner = malloc(sizeof(CatEvent) + sizeof(CatMessageInner));
    if (NULL == pEventInner) {
        return NULL;
    }
    CatEvent *pEvent = (CatEvent *) ((char *) pEventInner + sizeof(CatMessageInner));
    initCatMessage(pEvent, CatMessageType_Event, type, name);

    pEvent->complete = setEventComplete;
    return pEvent;
}

CatMetric *createCatMetric(const char *type, const char *name) {
    CatMessageInner *pMetricInner = malloc(sizeof(CatMetric) + sizeof(CatMessageInner));
    if (NULL == pMetricInner) {
        return NULL;
    }
    CatMetric *pMetric = (CatMetric *) ((char *) pMetricInner + sizeof(CatMessageInner));
    initCatMessage(pMetric, CatMessageType_Metric, type, name);

    pMetric->complete = setMetricComplete;
    return pMetric;
}

CatHeartBeat *createCatHeartBeat(const char *type, const char *name) {
    CatMessageInner *pHeartBeatInner = malloc(sizeof(CatHeartBeat) + sizeof(CatMessageInner));
    if (NULL == pHeartBeatInner) {
        return NULL;
    }
    CatHeartBeat *pHB = (CatHeartBeat *) ((char *) pHeartBeatInner + sizeof(CatMessageInner));

    initCatMessage(pHB, CatMessageType_HeartBeat, type, name);

    pHB->complete = setHeartBeatComplete;
    return pHB;
}
