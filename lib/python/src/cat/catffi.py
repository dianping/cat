# -*- coding: utf-8 -*-

import cffi

ffi = cffi.FFI()

definitions = """
typedef struct _CatMessage CatMessage;
typedef struct _CatMessage CatEvent;
typedef struct _CatMessage CatMetric;
typedef struct _CatMessage CatHeartBeat;

typedef struct _CatTranscation CatTransaction;

struct _CatTranscation {
    void (*addData)(CatTransaction *transaction, const char *data);

    void (*addKV)(CatTransaction *transaction, const char *dataKey, const char *dataValue);

    void (*setStatus)(CatTransaction *transaction, const char *status);

    void (*setTimestamp)(CatTransaction *transaction, unsigned long long timestamp);

    void (*complete)(CatTransaction *transaction);

    void (*addChild)(CatTransaction *transaction, CatMessage *message);

    void (*setDurationInMillis)(CatTransaction* transaction, unsigned long long duration);

    void (*setDurationStart)(CatTransaction* transaction, unsigned long long durationStart);
};

struct _CatMessage {
    void (*addData)(CatMessage *message, const char *data);

    void (*addKV)(CatMessage *message, const char *dataKey, const char *dataValue);

    void (*setStatus)(CatMessage *message, const char *status);

    void (*setTimestamp)(CatMessage *message, unsigned long long timestamp);

    void (*complete)(CatMessage *message);
};

typedef struct _CatClientConfig {
    int encoderType;
    int enableHeartbeat;
    int enableSampling;
    int enableMultiprocessing;
    int enableDebugLog;
} CatClientConfig;
"""

ffi.cdef(definitions)

# common apis.
ffi.cdef("int catClientInitWithConfig(const char *domain, CatClientConfig* config);")
ffi.cdef("int catClientDestory();")
ffi.cdef("int isCatEnabled();")

# transaction apis.
ffi.cdef("CatTransaction *newTransaction(const char *type, const char *name);")

# event apis.
ffi.cdef("void logEvent(const char *type, const char *name, const char *status, const char *nameValuePairs);")
ffi.cdef("void logError(const char *msg, const char *errStr);")

# heartbeat apis.
ffi.cdef("CatHeartBeat *newHeartBeat(const char *type, const char *name);")


# metric apis.
ffi.cdef("void logMetricForCount(const char *name, int quantity);")
ffi.cdef("void logMetricForDuration(const char *name, unsigned long long durationMs);")
