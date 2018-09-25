#ifndef CAT_CLIENT_C_CLIENT_H
#define CAT_CLIENT_C_CLIENT_H

#include "client_common.h"
#include "heartbeat.h"
#include "event.h"
#include "trace.h"
#include "metric.h"
#include "metric_helper.h"
#include "transaction.h"

#define CAT_OK 1
#define CAT_ERR 0

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Client Apis
 */

CATCLIENT_EXPORT int catSetLanguageBinding(const char* language, const char* client_version);

CATCLIENT_EXPORT int catDisableHeartbeat();

/**
 * Designed for python / nodejs which run multiple instances on same machine.
 */
CATCLIENT_EXPORT void catClientInitWithSingleProcessModel(); // for nodejs

CATCLIENT_EXPORT int catClientInit(const char *domain);

CATCLIENT_EXPORT int catClientDestroy();

/**
 * Transaction Apis
 */

CATCLIENT_EXPORT CatTransaction *newTransaction(const char *type, const char *name);

CATCLIENT_EXPORT CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long durationMs);

CATCLIENT_EXPORT void newCompletedTransactionWithDuration(const char *type, const char*name, unsigned long long durationMs);

CATCLIENT_EXPORT void logTransaction(const char *type, const char *name, const char *status, const char *nameValuePairs, unsigned long long durationMs);

CATCLIENT_EXPORT void logBatchTransaction(const char *type, const char *name, int count, int error, unsigned long long sum);

CATCLIENT_EXPORT void setTransactionStatus(CatTransaction *transaction, const char *status);

CATCLIENT_EXPORT void setTransactionTimestamp(CatTransaction *transaction, unsigned long long timestampMs);

CATCLIENT_EXPORT void completeTransaction(CatTransaction *transaction);

CATCLIENT_EXPORT void addTransactionDataPair(CatTransaction *transaction, const char *data);

/**
 * Message Common Apis
 */

CATCLIENT_EXPORT void setMessageStatus(CatMessage *message, const char *status);

CATCLIENT_EXPORT void setMessageTimestamp(CatMessage *message, unsigned long long timestampMs);

CATCLIENT_EXPORT void completeMessage(CatMessage *message);

CATCLIENT_EXPORT void addMessageData(CatHeartBeat *message, const char *data);

/**
 * Event Apis
 */

CATCLIENT_EXPORT CatEvent *newEvent(const char *type, const char *name);

CATCLIENT_EXPORT void logEvent(const char *type, const char *name, const char *status, const char *nameValuePairs);

CATCLIENT_EXPORT void logBatchEvent(const char *type, const char *name, int count, int error);

CATCLIENT_EXPORT void logEventWithTime(const char *type, const char *name, const char *status, const char *nameValuePairs, unsigned long long durationMs);

CATCLIENT_EXPORT void logError(const char *msg, const char *errStr);

/**
 * Heartbeat Apis
 */

CATCLIENT_EXPORT CatHeartBeat *newHeartBeat(const char *type, const char *name);

/**
 * @deprecated
 */
CATCLIENT_EXPORT void inline setHeartbeatStatus(CatHeartBeat *heartBeat, const char *status) {
    setMessageStatus(heartBeat, status);
}

/**
 * @deprecated
 */
CATCLIENT_EXPORT void inline completeHeartbeat(CatHeartBeat *heartBeat) {
    completeMessage(heartBeat);
}

/**
 * @deprecated
 */
CATCLIENT_EXPORT void inline addHeartbeatDataPair(CatHeartBeat *heartBeat, const char *data) {
    addMessageData(heartBeat, data);
}

/**
 * Metric Apis
 */

CATCLIENT_EXPORT CatMetric *newMetric(const char *type, const char *name);

CATCLIENT_EXPORT void logMetricForCount(const char *name);

CATCLIENT_EXPORT void logMetricForCountQuantity(const char *name, int quantity);

CATCLIENT_EXPORT void logMetricForDuration(const char *name, unsigned long long durationMs);

CATCLIENT_EXPORT void logMetricForLatestValue(const char *name, int quantity);

CATCLIENT_EXPORT void addMetricTag(CatMetricHelper *pHelper, const char *key, const char *val);

CATCLIENT_EXPORT void addMetricName(CatMetricHelper *pHelper, const char *name);

CATCLIENT_EXPORT void addMetricCount(CatMetricHelper *pHelper, int count);

CATCLIENT_EXPORT void addMetricDuration(CatMetricHelper *pHelper, unsigned long long durationMs);

/**
 * Trace Apis
 */

#define logTrace(type, name, status, nameValuePairs) logTraceWithCodeLocation((type), (name), (status), (nameValuePairs), \
    __FILE__, __FUNCTION__, __LINE__)

CATCLIENT_EXPORT void logTraceWithCodeLocation(const char *type, const char *name, const char *status,
                                               const char *nameValuePairs, const char *fileName,
                                               const char *funcationName, int lineNo);

#define logErrorTrace(type, name, data) logErrorWithCodeLocation((type), (name), (data), \
    __FILE__, __FUNCTION__, __LINE__)

CATCLIENT_EXPORT void logErrorWithCodeLocation(const char *type, const char *name,
                                               const char *data, const char *fileName, const char *funcationName,
                                               int lineNo);

/**
 * Message Id Apis
 */

CATCLIENT_EXPORT char *createMessageId();

CATCLIENT_EXPORT char *createRemoteServerMessageId(const char *domain);

CATCLIENT_EXPORT char *getThreadLocalMessageTreeId();

CATCLIENT_EXPORT char *getThreadLocalMessageTreeRootId();

CATCLIENT_EXPORT char *getThreadLocalMessageTreeParentId();

CATCLIENT_EXPORT void setThreadLocalMessageTreeId(char *messageId, char *rootMessageId, char *parentMessageId);

#ifdef __cplusplus
}
#endif

#endif //CAT_CLIENT_C_CLIENT_H
