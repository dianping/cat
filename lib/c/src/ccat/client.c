#include "ccat/client.h"

#include <lib/cat_clog.h>
#include <lib/cat_time_util.h>

#include "client_config.h"
#include "context.h"
#include "message.h"
#include "message_aggregator.h"
#include "message_aggregator_metric.h"
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

volatile sds g_single_process_pid_str = NULL;

extern volatile int g_cat_enabledFlag;

extern CatMessage g_cat_nullMsg;
extern CatTransaction g_cat_nullTrans;

CatClientConfig DEFAULT_CCAT_CONFIG = {
        CAT_ENCODER_BINARY,
        1,  // enable heartbeat
        1,  // enable sampling
        0,  // disable multiprocessing
        0,  // disable debug log
};

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

    if (loadCatClientConfig(DEFAULT_XML_FILE) < 0) {
        g_cat_init = 0;
        g_cat_enabledFlag = 0;
        INNER_LOG(CLOG_ERROR, "Failed to initialize cat: Error occurred while parsing client config.");
        return 0;
    }
    g_config.appkey = catsdsnew(appkey);

    initMessageManager(appkey, g_config.selfHost);
    initMessageIdHelper();

    if (!initCatServerConnManager()) {
        g_cat_init = 0;
        g_cat_enabledFlag = 0;
        INNER_LOG(CLOG_ERROR, "Failed to initialize cat: Error occurred while getting router from cat-server.");
        return 0;
    }

    initCatAggregatorThread();
    initCatSenderThread();
    initCatMonitorThread();

    g_cat_enabledFlag = 1;
    INNER_LOG(CLOG_INFO, "Cat has been successfully initialized with appkey: %s", appkey);

    return 1;
}

int catClientDestroy() {
    g_cat_enabledFlag = 0;
    g_cat_init = 0;

    clearCatMonitor();
    catMessageManagerDestroy();
    clearCatAggregatorThread();
    clearCatSenderThread();
    clearCatServerConnManager();
    destroyMessageIdHelper();
    clearCatClientConfig();
    return 1;
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
    getCatContext();
    CatEvent *event = createCatEvent(type, name);
    catChecktPtr(event);
    return event;
}

CatMetric *newMetric(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullMsg;
    }
    getCatContext();
    CatMetric *metric = createCatMetric(type, name);
    catChecktPtr(metric);
    return metric;
}

CatHeartBeat *newHeartBeat(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullMsg;
    }
//    getCatContext();
    getContextMessageTree()->canDiscard = 0;

    CatHeartBeat *hb = createCatHeartBeat(type, name);
    catChecktPtr(hb);
    return hb;
}

CatTransaction *newTransaction(const char *type, const char *name) {
    if (!isCatEnabled()) {
        return &g_cat_nullTrans;
    }
    getCatContext();
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
