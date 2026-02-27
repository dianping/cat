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
#ifndef CCAT_CAT_CONDITION_H
#define CCAT_CAT_CONDITION_H

#include <pthread.h>
#include <sys/time.h>
#include <stdio.h>

typedef struct _CatCondition {
    pthread_mutex_t mutex;
    pthread_cond_t cond;
} CatCondition;

static inline void CatConditionInit(CatCondition *cond) {
    pthread_condattr_t condattr = {0};
    pthread_cond_init(&cond->cond, &condattr);
    pthread_mutex_init(&cond->mutex, NULL);
}

static inline void CatConditionDestory(CatCondition *cond) {
    pthread_cond_destroy(&cond->cond);
    pthread_mutex_destroy(&cond->mutex);
}

static inline void CatConditionSignal(CatCondition *cond) {
    pthread_mutex_lock(&cond->mutex);
    pthread_cond_signal(&cond->cond);
    pthread_mutex_unlock(&cond->mutex);
}

/**
 *
 * @param cond
 * @param remain in nanoseconds.
 * @return
 */
static inline long CatConditionWait(CatCondition *cond, long remainInMillis) {
    struct timeval t1, t2;
    gettimeofday(&t1, NULL);
    pthread_mutex_lock(&cond->mutex);
    struct timespec ts = {
            t1.tv_sec + remainInMillis / 1000,
            t1.tv_usec * 1000 + remainInMillis % 1000 * 1000 * 1000
    };
    pthread_cond_timedwait(&cond->cond, &cond->mutex, &ts);

    pthread_mutex_unlock(&cond->mutex);
    gettimeofday(&t2, NULL);

    long delta = (t2.tv_sec - t1.tv_sec) * 1000 + (t2.tv_usec - t1.tv_usec) / 1000;
    return remainInMillis - delta;
}

#endif //CCAT_CAT_CONDITION_H
