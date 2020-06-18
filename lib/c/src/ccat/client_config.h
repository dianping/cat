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
#ifndef CAT_CLIENT_C_CLIENT_CONFIG_H
#define CAT_CLIENT_C_CLIENT_CONFIG_H

#include "client.h"

#include "lib/cat_sds.h"

#define catChecktPtr(ptr) catChecktPtrWithName((ptr), (#ptr))

typedef struct _CatClientInnerConfig {
    sds appkey;
    sds selfHost;
    sds serverHost;

    sds defaultIp;
    sds defaultIpHex;

    unsigned int serverPort;
    int serverNum;
    sds *serverAddresses;

    int messageEnableFlag;
    int messageQueueSize;
    int messageQueueBlockPrintCount;
    int maxChildSize;
    int maxContextElementSize;

    int logFlag;
    int logSaveFlag;
    int logDebugFlag;
    int logFileWithTime;
    int logFilePerDay;
    int logLevel;

    sds configDir;
    sds dataDir;
    sds indexFileName;

    int encoderType;
    volatile int enableHeartbeat;
    volatile int enableSampling;
    volatile int enableMultiprocessing;
    volatile int enableAutoInitialize;

} CatClientInnerConfig;

#define DEFAULT_APPKEY "cat"
#define DEFAULT_IP "127.0.0.1"
#define DEFAULT_IP_HEX "7f000001"

// #define DEFAULT_XML_FILE "/data/appdatas/cat/client.xml"

// 通过指定环境变量CAT_HOME来修改此路径
#define DEFAULT_CAT_HOME "/data/appdatas/cat/"

// #if defined(__linux__) || defined(__APPLE__)
// #define DEFAULT_DATA_DIR "/data/appdatas/cat/"
// #else
// #define DEFAULT_DATA_DIR "./"
// #endif

extern CatClientInnerConfig g_config;

int loadCatClientConfig(const char *filename);

void initCatClientConfig(CatClientConfig *config);

void clearCatClientConfig();

void catChecktPtrWithName(void *ptr, char *ptrName);

char *catHome();

#endif //CAT_CLIENT_C_CLIENT_CONFIG_H
