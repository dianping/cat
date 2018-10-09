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
#ifndef CAT_CLIENT_C_ENCODER_H
#define CAT_CLIENT_C_ENCODER_H

#include "ccat/message_tree.h"

#include "lib/cat_sds.h"

#define CAT_ENCODER_TEXT 0
#define CAT_ENCODER_BINARY 1

typedef struct _CatEncoder CatEncoder;

struct _CatEncoder {
    void (*setAppkey)(CatEncoder *encoder, const char *appkey);

    void (*setHostname)(CatEncoder *encoder, const char *hostname);

    void (*setIp)(CatEncoder *encoder, const char *ip);

    void (*header)(CatEncoder *encoder, CatMessageTree *tree);

    void (*message)(CatEncoder *encoder, CatMessage *message);

    void (*transactionStart)(CatEncoder *encoder, CatTransaction *t);

    void (*transactionEnd)(CatEncoder *encoder, CatTransaction *t);

    void (*transaction)(CatEncoder *encoder, CatTransaction *t);

    void (*event)(CatEncoder *encoder, CatEvent *e);

    void (*metric)(CatEncoder *encoder, CatMetric *m);

    void (*heartbeat)(CatEncoder *encoder, CatHeartBeat *h);

    const char *ip;
    const char *hostname;
    const char *appkey;

    sds* buf;
};

void catEncodeMessageTree(CatMessageTree *tree, sds buf);

CatEncoder *newCatEncoder();

CatEncoder *newCatTextEncoder();

CatEncoder *newCatBinaryEncoder();

void deleteCatEncoder(CatEncoder *encoder);

#endif // CAT_CLIENT_C_ENCODER_H