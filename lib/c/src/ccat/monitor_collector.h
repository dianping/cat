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
#ifndef CCAT_MONITOR_COLLECTOR_H
#define CCAT_MONITOR_COLLECTOR_H

#include <string.h>
#include <ctype.h>

#include "lib/cat_ezxml.h"

#define startswith(line, prefix) (strncmp(line, prefix, sizeof(prefix) - 1) == 0)
#define ldtoa(l) (sprintf(buf, "%ld", l), buf)
#define lftoa(lf) (sprintf(buf, "%lf", lf), buf)

#define LINE_BUF_SIZE 256
#define MEM_TOTAL_PREFIX        "MemTotal:"
#define MEM_FREE_PREFIX         "MemFree:"
#define MEM_CACHED_PREFIX       "Cached:"
#define MEM_SWAP_TOTAL_PREFIX   "SwapTotal:"
#define MEM_SWAP_FREE_PREFIX    "SwapFree:"

typedef struct loadavg {
    double avg1;
    double avg5;
    double avg15;
} loadavg;

typedef struct cpustat {
    long user;
    long nice;
    long system;
    long idle;
    long iowait;
    long irq;
    long softirq;

    long intr;
    long context;

    long procRunning;
    long procBlocked;
} cpustat;

typedef struct cpuinfo {
    long user;
    long nice;
    long system;
    long idle;
    long iowait;
    long irq;
    long softirq;
    long total;

    long intr;
    long context;
} cpuinfo;

typedef struct meminfo {
    long memTotal;
    long memFree;
    long memCached;
    long swapTotal;
    long swapFree;
} meminfo;

static void add_detail(ezxml_t ext, const char *key, const char *val, int *index);

char *get_status_report();

#endif //CCAT_MONITOR_COLLECTOR_H
