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
#ifndef CAT_CLIENT_C_CLIENT_H
#define CAT_CLIENT_C_CLIENT_H

/**
 * Constants
 */

#define CAT_CLIENT_EXPORT

#define CAT_SUCCESS "0"
#define CAT_FAIL "-1"
#define CAT_ERROR "ERROR"

#define CAT_ENCODER_TEXT 0
#define CAT_ENCODER_BINARY 1

/**
 * C Struct Definitions
 */
typedef struct _CatMessage CatMessage;
typedef struct _CatMessage CatEvent;
typedef struct _CatMessage CatMetric;
typedef struct _CatMessage CatHeartBeat;

typedef struct _CatTransaction CatTransaction;

struct _CatMessage {
    /**
     * Add some debug data to a message.
     * It will be shown in the log view page.
     * Note not all the data will be preserved. (If sampling is enabled)
     */
    void (*addData)(CatMessage *message, const char *data);

    /**
     * Add some debug data to a message, in key-value format.
     */
    void (*addKV)(CatMessage *message, const char *dataKey, const char *dataValue);

    /**
     * Set the status of a message
     * Note that any status not equal to "0" (CAT_SUCCESS) will be treated as a "problem".
     * And can be recorded in our problem report.
     * A message tree which contains a "problem" message won't be sampling.
     * In some cases, especially in high concurrency situations, it may cause network problems.
     */
    void (*setStatus)(CatMessage *message, const char *status);

    /**
     * Set the created timestamp of a transaction
     */
    void (*setTimestamp)(CatMessage *message, unsigned long long timestamp);

    /**
     * Complete the message.
     * Meaningless in most cases, only transaction has to be completed manually.
     */
    void (*complete)(CatMessage *message);
};

struct _CatTransaction {
    /**
     * Add some debug data to a transaction.
     * It will be shown in the log view page.
     * Note not all the data will be preserved. (If sampling is enabled)
     */
    void (*addData)(CatTransaction *transaction, const char *data);

    /**
     * Add some debug data to a transaction, in key-value format.
     */
    void (*addKV)(CatTransaction *transaction, const char *dataKey, const char *dataValue);

    /**
     * Set the status of a transaction
     * Note that any status not equal to "0" (CAT_SUCCESS) will be treated as a "problem".
     * And can be recorded in our problem report.
     * A message tree which contains a "problem" transaction won't be sampling.
     * In some cases, especially in high concurrency situations, it may cause network problems.
     */
    void (*setStatus)(CatTransaction *transaction, const char *status);

    /**
     * Set the created timestamp of a transaction
     */
    void (*setTimestamp)(CatTransaction *transaction, unsigned long long timestamp);

    /**
     * Complete the transaction
     */
    void (*complete)(CatTransaction *transaction);

    /**
     * Add a child directly to a transaction.
     * Avoid of using this api unless you really have to do so.
     */
    void (*addChild)(CatTransaction *transaction, CatMessage *message);

    /**
     * Set the duration of a transaction.
     * The duration will be calculated when the transaction has been completed.
     * You can prevent it and specified the duration through this api.
     */
    void (*setDurationInMillis)(CatTransaction* transaction, unsigned long long duration);

    /**
     * Set the duration start of a transaction.
     * The duration start is same as timestamp by default.
     * You can overwrite it through this api, which can influence the calculated duration.
     * When a transaction is completed, the duration will be set to (currentTimestamp - durationStart)
     * Note that it only works when duration has not been specified.
     */
    void (*setDurationStart)(CatTransaction* transaction, unsigned long long durationStart);
};

typedef struct _CatClientConfig {
    int encoderType;
    int enableHeartbeat;
    int enableSampling;
    int enableMultiprocessing;
    int enableDebugLog;
    int enableAutoInitialize;
} CatClientConfig;

extern CatClientConfig DEFAULT_CCAT_CONFIG;

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Common Apis
 */
CAT_CLIENT_EXPORT int catClientInit(const char *appkey);

CAT_CLIENT_EXPORT int catClientInitWithConfig(const char *appkey, CatClientConfig* config);

CAT_CLIENT_EXPORT int catClientDestroy();

CAT_CLIENT_EXPORT const char* catVersion();

CAT_CLIENT_EXPORT int isCatEnabled();

/**
 * Transaction Apis
 */
CAT_CLIENT_EXPORT CatTransaction *newTransaction(const char *type, const char *name);

/**
 * Create a transaction with specified duration in milliseconds.
 *
 * This is equivalent to
 *
 * CatTransaction *t = newTransaction("type", "name");
 * t->setDurationInMillis(t4, 1000);
 * return t;
 */
CAT_CLIENT_EXPORT CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long duration);

/**
 * Log a transaction with specified duration in milliseconds.
 *
 * Due to the transaction has been auto completed,
 * the duration start and the created timestamp will be turn back.
 *
 * This is equivalent to
 *
 * CatTransaction *t = newTransaction("foo", "bar2-completed");
 * t->setTimestamp(t, GetTime64() - 1000);
 * t->setDurationStart(t, GetTime64() - 1000);
 * t->setDurationInMillis(t, 1000);
 * t->complete(t);
 * return;
 */
CAT_CLIENT_EXPORT void newCompletedTransactionWithDuration(const char *type, const char *name, unsigned long long duration);

/**
 * Event Apis
 */
CAT_CLIENT_EXPORT void logEvent(const char *type, const char *name, const char *status, const char *data);

/**
 * Log a error message.
 *
 * This is equivalent to
 *
 * logEvent("Exception", msg, CAT_ERROR, errStr);
 */
CAT_CLIENT_EXPORT void logError(const char *msg, const char *errStr);

/**
 * Create a event message manually.
 *
 * Avoid using this api unless you really have to.
 */
CAT_CLIENT_EXPORT CatEvent *newEvent(const char *type, const char *name);

/**
 * Heartbeat Apis
 */

/**
 * Create a heartbeat message manually.
 *
 * Heartbeat is reported by cat client automatically,
 * so you don't have to use this api in most cases,
 * unless you want to overwrite our heartbeat message.
 *
 * Don't forget to disable our built-in heartbeat if you do so.
 */
CAT_CLIENT_EXPORT CatHeartBeat *newHeartBeat(const char *type, const char *name);

/**
 * Metric Apis
 */
CAT_CLIENT_EXPORT void logMetricForCount(const char *name, int quantity);

CAT_CLIENT_EXPORT void logMetricForDuration(const char *name, unsigned long long duration);

/**
 * MessageId Apis
 */
CAT_CLIENT_EXPORT char *createMessageId();

CAT_CLIENT_EXPORT char *createRemoteServerMessageId(const char *appkey);

CAT_CLIENT_EXPORT char *getThreadLocalMessageTreeId();

CAT_CLIENT_EXPORT char *getThreadLocalMessageTreeRootId();

CAT_CLIENT_EXPORT char *getThreadLocalMessageTreeParentId();

CAT_CLIENT_EXPORT void setThreadLocalMessageTreeId(char *messageId);

CAT_CLIENT_EXPORT void setThreadLocalMessageTreeRootId(char *messageId);

CAT_CLIENT_EXPORT void setThreadLocalMessageTreeParentId(char *messageId);

#ifdef __cplusplus
}
#endif

#endif //CAT_CLIENT_C_CLIENT_H
