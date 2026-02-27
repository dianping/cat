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
#ifndef CCAT_CAT_MPSC_QUEUE_H
#define CCAT_CAT_MPSC_QUEUE_H

#include "cat_atomic.h"
#include "cat_condition.h"
#include "cat_sds.h"
#include "cat_semaphore.h"

typedef struct _queue {
    sds name;
} CatMPSCQueue;

#ifdef __cplusplus
extern "C" {
#endif

CatMPSCQueue *newCatMPSCQueue(const char *name, int capacity);

void deleteCatMPSCQueue(CatMPSCQueue *q);

int CatMPSC_offer(CatMPSCQueue *q, void *data);

void *CatMPSC_poll(CatMPSCQueue *q);

int CatMPSC_boffer(CatMPSCQueue *q, void *data, int ms);

void *CatMPSC_bpoll(CatMPSCQueue *q, int ms);

int CatMPSC_size(CatMPSCQueue *q);

int CatMPSC_capacity(CatMPSCQueue *q);

#ifdef __cplusplus
}
#endif

#endif //CCAT_CAT_MPSC_QUEUE_H
