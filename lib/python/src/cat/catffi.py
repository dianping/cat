#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com>

# Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
    int enableAutoInitialize;
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
