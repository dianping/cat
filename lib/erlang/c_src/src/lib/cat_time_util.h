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
#ifndef CAT_TIME_UTIL_H
#define CAT_TIME_UTIL_H

#include "typedef.h"
#include "headers.h"

#define Sleep(ms) usleep((ms) * 1000);

/**
 * Get current timestamp in 64 bit (milliseconds since midnight, Jan 1, 1970 UTC)
 */
static u_int64 inline GetTime64() {
    u_int64 buf;
#if defined(WIN32)
    struct __timeb64 timeBuf;
    _ftime64_s(&timeBuf);
    buf = (timeBuf.time * 1000) + (timeBuf.millitm);
#elif defined(__linux__) || defined(__APPLE__)
    struct timeval tv;
    gettimeofday(&tv, NULL);
    buf = tv.tv_sec * 1000 + tv.tv_usec / 1000;
#endif
    return buf;
}

char *GetDetailTimeString(u_int64 srcTime);

char *GetCatTimeString(u_int64 srcTime);

typedef struct _TimeInterval {
    u_int64 time_s;
    u_int64 time_e;
} TimeInterval;

static inline u_int8 TimeIntervalCompare(TimeInterval *resInterval, TimeInterval *desInterval) {
    u_int8 rst = 0;
    if (resInterval->time_e < desInterval->time_s) {
        rst = 0;
    } else {
        //res.s > des.e no cross
        if (resInterval->time_s > desInterval->time_e) {
            rst = 0;
        } else {
            if (resInterval->time_s > desInterval->time_s) {
                if (resInterval->time_e > desInterval->time_e) {
                    //res                             |________|
                    //des                    |__________|
                    rst = 1;
                } else {
                    //res                     |__________|
                    //des                |_________________|
                    rst = 2;
                }
            } else {
                if (resInterval->time_e > desInterval->time_e) {
                    //res              |____________|
                    //des                  |_______|
                    rst = 3;
                } else {
                    //res              |___________|
                    //des                 |_____________|
                    rst = 4;
                }
            }
        }
    }
    return rst;
}

static inline unsigned long long catTrimToHour(unsigned long long timeMs) {
    return timeMs / (3600 * 1000);
}

#endif // CAT_TIME_UTIL_H
