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
#ifndef CCAT_CAT_SEMAPHORE_H
#define CCAT_CAT_SEMAPHORE_H

#ifdef WIN32

#include <semaphore.h>

typedef HANDLE SEMA;
#define SEMA_INIT(sema, initCount, maxCount) sema = CreateSemaphore(NULL, initCount, maxCount, NULL)
#define SEMA_POST(sema) ReleaseSemaphore(sema, 1, NULL)
#define SEMA_WAIT(sema) WaitForSingleObject(sema, INFINITE)
#define SEMA_WAIT_TIME(sema, delay) WaitForSingleObject(sema, delay)
#define SEMA_TRYWAIT(sema) WaitForSingleObject(sema, 0)
#define SEMA_DESTROY(sema) CloseHandle(sema)

#define SEMA_WAIT_OK WAIT_OBJECT_0

#elif defined(__linux)

#include <semaphore.h>
#include <sys/time.h>
#include <time.h>

static inline int sema_wait_time_(sem_t* sema, unsigned int delay)
{
    struct timespec ts;

    struct timeval tv;

    gettimeofday(&tv, NULL);

    tv.tv_usec += (delay % 1000) * 1000;
    tv.tv_sec += delay / 1000;
    if (tv.tv_usec > 1000000) {
        tv.tv_usec -= 1000000;
        ++tv.tv_sec;
    }

    ts.tv_sec = tv.tv_sec;
    ts.tv_nsec = tv.tv_usec * 1000;

    return sem_timedwait(sema, &ts);
}

#define SEMA sem_t
#define SEMA_INIT(sema, initCount, maxCount) sem_init(&sema,0,initCount)
#define SEMA_POST(sema) sem_post(&sema)
#define SEMA_WAIT(sema) sem_wait(&sema)
#define SEMA_WAIT_TIME(sema,delay) sema_wait_time_(&sema,delay)
#define SEMA_TRYWAIT(sema) sem_trywait(&sema)
#define SEMA_DESTROY(sema) sem_destroy(&sema)

#define SEMA_WAIT_OK 0

#elif defined(__APPLE__)

#include <dispatch/dispatch.h>

static inline dispatch_time_t sema_delay_(unsigned int delay) {
    return dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC / 1000);
}

#define SEMA dispatch_semaphore_t
#define SEMA_INIT(sema, initCount, maxCount) sema = dispatch_semaphore_create(initCount);
#define SEMA_POST(sema) dispatch_semaphore_signal(sema);
#define SEMA_WAIT(sema) dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER)
#define SEMA_WAIT_TIME(sema, delay) dispatch_semaphore_wait(sema, sema_delay_(delay))
#define SEMA_TRYWAIT(sema) dispatch_semaphore_wait(sema, DISPATCH_TIME_NOW)
#define SEMA_DESTROY(sema)

#define SEMA_WAIT_OK 0

#endif

#endif //CCAT_CAT_SEMAPHORE_H
