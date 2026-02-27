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
#include "monitor_collector.h"

#include "client.h"


void inline add_detail(ezxml_t ext, const char *key, const char *val, int *index) {
    ezxml_t detail = ezxml_add_child_d(ext, "extensionDetail", *index);
    ezxml_set_attr_d(detail, "id", key);
    ezxml_set_attr_d(detail, "value", val);
    (*index)++;
}

loadavg *fetch_loadavg() {
    loadavg *info = calloc(1, sizeof(loadavg));
    FILE *fp;

    if ((fp = fopen("/proc/loadavg", "r")) == NULL) {
        // TODO cat log error
        return info;
    }

    fscanf(fp, "%lf %lf %lf", &info->avg1, &info->avg5, &info->avg15);

    fclose(fp);
    return info;
}

cpustat *fetch_cpustat() {
    cpustat *stat = calloc(1, sizeof(cpustat));
    FILE *fp;

    if ((fp = fopen("/proc/stat", "r")) == NULL) {
        return stat;
    }

    char line[LINE_BUF_SIZE];
    while (fgets(line, LINE_BUF_SIZE, fp) != NULL) {
        if (startswith(line, "cpu") && isblank(line[3])) {  // 3 is sizeof("cpu") - 1
            int code = sscanf(
                    line,
                    "%*s %ld %ld %ld %ld %ld %ld %ld",
                    &stat->user,
                    &stat->nice,
                    &stat->system,
                    &stat->idle,
                    &stat->iowait,
                    &stat->irq,
                    &stat->softirq
            );
            if (0 == code) {
                // TODO log error
                return stat;
            }
        } else if (startswith(line, "intr")) {
            sscanf(line, "%*s %ld", &stat->intr);
        } else if (startswith(line, "ctxt")) {
            sscanf(line, "%*s %ld", &stat->context);
        } else if (startswith(line, "procs_running")) {
            sscanf(line, "%*s %ld", &stat->procRunning);
        } else if (startswith(line, "procs_blocked")) {
            sscanf(line, "%*s %ld", &stat->procBlocked);
        }
    }

    fclose(fp);
    return stat;
}

cpuinfo *cpustat_delta(cpustat *old, cpustat *new) {
    cpuinfo *info = calloc(1, sizeof(cpuinfo));
    info->user = new->user - old->user;
    info->nice = new->nice - old->nice;
    info->system = new->system - old->system;
    info->idle = new->idle - old->idle;
    info->iowait = new->iowait - old->iowait;
    info->irq = new->irq - old->irq;
    info->softirq = new->softirq - old->softirq;
    info->total = info->user + info->nice + info->system + info->idle + info->iowait + info->irq + info->softirq;

    info->intr = new->intr - old->intr;
    info->context = new->context - old->context;
    return info;
}

meminfo *fetch_meminfo() {
    meminfo *info = calloc(1, sizeof(meminfo));

    FILE *fp;
    if ((fp = fopen("/proc/meminfo", "r")) == NULL) {
        // TODO cat log error
        return info;
    }

    char line[LINE_BUF_SIZE];
    long value;

    while (fgets(line, sizeof(line), fp) != NULL) {
        if (sscanf(line, "%*s %ld kB", &value) != 1) {
            continue;
        }
        if (startswith(line, MEM_TOTAL_PREFIX)) {
            info->memTotal = value * 1024;
        } else if (startswith(line, MEM_FREE_PREFIX)) {
            info->memFree = value * 1024;
        } else if (startswith(line, MEM_CACHED_PREFIX)) {
            info->memCached = value * 1024;
        } else if (startswith(line, MEM_SWAP_TOTAL_PREFIX)) {
            info->swapTotal = value * 1024;
        } else if (startswith(line, MEM_SWAP_FREE_PREFIX)) {
            info->swapFree = value * 1024;
        }
    }

    fclose(fp);
    return info;
}

cpustat *previous_stat = NULL;

void add_cpuinfo(ezxml_t ext, int *count) {
    char *buf = malloc(16);

    loadavg *avg = fetch_loadavg();
    add_detail(ext, "system.load.average", lftoa(avg->avg1), count);
    add_detail(ext, "load.1min", lftoa(avg->avg1), count);
    add_detail(ext, "load.5min", lftoa(avg->avg5), count);
    add_detail(ext, "load.15min", lftoa(avg->avg15), count);
    free(avg);

    cpustat *stat = fetch_cpustat();
    add_detail(ext, "process.running", ldtoa(stat->procRunning), count);
    add_detail(ext, "process.blocked", ldtoa(stat->procBlocked), count);

    if (NULL != previous_stat) {
        cpuinfo *info = cpustat_delta(previous_stat, stat);
        add_detail(ext, "cpu.user", ldtoa(info->user), count);
        add_detail(ext, "cpu.nice", ldtoa(info->nice), count);
        add_detail(ext, "cpu.system", ldtoa(info->system), count);
        add_detail(ext, "cpu.idle", ldtoa(info->idle), count);
        add_detail(ext, "cpu.iowait", ldtoa(info->iowait), count);
        add_detail(ext, "cpu.irq", ldtoa(info->irq), count);
        add_detail(ext, "cpu.softirq", ldtoa(info->softirq), count);

        if (info->total > 0) {
            add_detail(ext, "cpu.user.percent", lftoa((double) (info->user) / info->total * 100), count);
            add_detail(ext, "cpu.nice.percent", lftoa((double) (info->nice) / info->total * 100), count);
            add_detail(ext, "cpu.system.percent", lftoa((double) (info->system) / info->total * 100), count);
            add_detail(ext, "cpu.idle.percent", lftoa((double) (info->idle) / info->total * 100), count);
            add_detail(ext, "cpu.iowait.percent", lftoa((double) (info->iowait) / info->total * 100), count);
            add_detail(ext, "cpu.irq.percent", lftoa((double) (info->irq) / info->total * 100), count);
            add_detail(ext, "cpu.softirq.percent", lftoa((double) (info->softirq) / info->total * 100), count);
        }

        add_detail(ext, "cpu.context", ldtoa(info->context), count);
        add_detail(ext, "cpu.intr", ldtoa(info->intr), count);

        free(info);
        free(previous_stat);
    }
    previous_stat = stat;

    free(buf);
}

void add_meminfo(ezxml_t ext, int *count) {
    meminfo *info = fetch_meminfo();
    char *buf = malloc(16);

    double memfree_percent = (double) info->memFree / info->memTotal * 100;
    double memused_percent = 100.0 - memfree_percent;

    add_detail(ext, "mem.memtotal", ldtoa(info->memTotal), count);
    add_detail(ext, "mem.memfree", ldtoa(info->memFree), count);
    add_detail(ext, "mem.memcached", ldtoa(info->memCached), count);
    add_detail(ext, "mem.swaptotal", ldtoa(info->swapTotal), count);
    add_detail(ext, "mem.swapfree", ldtoa(info->swapFree), count);
    add_detail(ext, "mem.memfree.percent", lftoa(memfree_percent), count);
    add_detail(ext, "mem.memused.percent", lftoa(memused_percent), count);

    free(buf);
    free(info);
}

void system_process(ezxml_t xml) {
    ezxml_t ext = ezxml_add_child_d(xml, "extension", 0);
    ezxml_set_attr_d(ext, "id", "system.process");

    ezxml_t desc = ezxml_add_child_d(ext, "description", 0);
    ezxml_set_txt_d(desc, "<![CDATA[system.process]]>");

    int count = 1;

    CatTransaction *t = newTransaction("System", "collect_cpuinfo");
    add_cpuinfo(ext, &count);
    t->complete(t);

    t = newTransaction("System", "collect_meminfo");
    add_meminfo(ext, &count);
    t->complete(t);
}

char *get_status_report() {
    ezxml_t xml = ezxml_new_d("status");

    CatTransaction *t = newTransaction("System", "collect_system_process");
    system_process(xml);
    t->complete(t);

    char *xmlContent = ezxml_toxml(xml);
    ezxml_free(xml);

    return xmlContent;
}
