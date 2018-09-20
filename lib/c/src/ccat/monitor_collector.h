//
// Created by Terence on 16/04/2018.
//

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
