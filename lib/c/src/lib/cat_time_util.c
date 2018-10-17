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
#include "cat_time_util.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#ifdef WIN32
#define THREADLOCAL __declspec(thread)
#elif defined(__linux__) || defined(__APPLE__)
#define THREADLOCAL __thread
#else
#define THREADLOCAL
#endif

char *GetTimeString(u_int64 srcTime) {

    /**
     * Explanation of struct tm:
     *
     * struct tm {
     *      int tm_sec;             // second - range [0, 60)
     *      int tm_min;             // minute - range [0, 60)
     *      int tm_hour;            // hour - range [0, 24)
     *      int tm_mday;            // day of month - range [0, 31]
     *      int tm_mon;             // month - range [0, 12)
     *      int tm_year;            // year - range [1900, +∞)
     *      int tm_wday;            // weekday - range [0, 7), 0 represents Sunday.
     *      int tm_yday;            // day of year - range [0, 365], 0 represents January 1st.
     *      int tm_isdst;           // is summer time (is daylight saving time).
     *      long int tm_gmtoff;     // The difference in seconds from UTC - range [-12 * 3600, 14 * 3600]
     *      const char *tm_zone;    // current time zone. (related to env variable TZ)
     * };
     *
     */

    time_t t = 0;
    if (srcTime == 0) {
        t = time(0);
    } else {
        t = srcTime / 1000;
    }

    static THREADLOCAL char *tmp = NULL;
    if (tmp == NULL) {
        tmp = (char *) malloc(128);
    }
#pragma warning( push )
#pragma warning( disable : 4996 )
    strftime(tmp, 64, "%Y-%m-%d %H-%M-%S", localtime(&t));
#pragma warning( pop )
    return tmp;
}

char *GetDetailTimeString(u_int64 srcTime) {
    time_t t = 0;
#if defined(WIN32)
    struct __timeb64 timeBuf;
    if (srcTime == 0)
    {
        t = time(0);
        _ftime64_s(&timeBuf);
    }
    else
    {
        t = srcTime / 1000;
        timeBuf.millitm = srcTime % 1000;
    }
#elif defined(__linux__) || defined(__APPLE__)

    struct timeval tv;

    if (srcTime == 0) {
        gettimeofday(&tv, NULL);
    } else {
        t = srcTime / 1000;
        tv.tv_usec = (srcTime % 1000) * 1000;
    }
#endif
    static THREADLOCAL char *tmp = NULL;
    if (tmp == NULL) {
        tmp = (char *) malloc(128);
    }
#pragma warning( push )
#pragma warning( disable : 4996 )
    strftime(tmp, 128, "%Y-%m-%d %H-%M-%S", localtime(&t));
#pragma warning( pop )
    size_t timeBufLen = strlen(tmp);
#if defined(WIN32)
    sprintf_s(tmp + timeBufLen, 128 - timeBufLen, "-%03d", timeBuf.millitm);
#else
    snprintf(tmp + timeBufLen, sizeof(tmp) - timeBufLen, "-%03d", (int) (tv.tv_usec / 1000));
#endif
    return tmp;
}

char *GetCatTimeString(u_int64 srcTime) {
    time_t t = 0;
#if defined(WIN32)
    struct __timeb64 timeBuf;
    if (srcTime == 0)
    {
        t = time(0);
        _ftime64_s(&timeBuf);
    }
    else
    {
        t = srcTime / 1000;
        timeBuf.millitm = srcTime % 1000;
    }
#elif defined(__linux__) || defined(__APPLE__)

    struct timeval tv;

    if (srcTime == 0) {
        gettimeofday(&tv, NULL);
    } else {
        t = srcTime / 1000;
        tv.tv_usec = (srcTime % 1000) * 1000;
    }
#endif
    static THREADLOCAL char *tmp = NULL;
    if (tmp == NULL) {
        tmp = (char *) malloc(128);
    }
#pragma warning( push )
#pragma warning( disable : 4996 )
    strftime(tmp, 128, "%Y-%m-%d %H:%M:%S", localtime(&t));
#pragma warning( pop )
    size_t timeBufLen = strlen(tmp);
#if defined(WIN32)
    sprintf_s(tmp + timeBufLen, 128 - timeBufLen, ".%03d", timeBuf.millitm);
#else
    snprintf(tmp + timeBufLen, sizeof(tmp) - timeBufLen, ".%03d", (int) (tv.tv_usec / 1000));
#endif
    return tmp;

}
