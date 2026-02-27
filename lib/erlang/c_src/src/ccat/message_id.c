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
#include "message_id.h"

#include "ccat/client_config.h"
#include "ccat/functions.h"
#include "ccat/message_manager.h"

#include "lib/cat_ccmap.h"
#include "lib/cat_time_util.h"

extern CatMessageManager g_cat_messageManager;

static volatile int g_id_index = 0;
static volatile sds g_multiprocessing_pid_str = NULL;
static volatile unsigned long long g_last_hour = 0;

static sds g_index_filePath = NULL;
static sds g_id_prefix = NULL; // "%s-%s-%ll-", g_cat_messageManager.appkey, g_cat_messageManager.ipHex, g_last_hour

static CATCRITICALSECTION g_id_lock = NULL;

static CCHashMap *g_domainMessageIdDict;

static void domainMessageIdFreeFun(void *privdata, void *val) {
    free((ATOMICLONG *) val);
}

extern unsigned int _dictStringCopyHTHashFunction(const void *key);

extern void *_dictStringCopyHTKeyDup(void *privdata, const void *key);

extern void *_dictStringKeyValCopyHTValDup(void *privdata, const void *val);

extern int _dictStringCopyHTKeyCompare(void *privdata, const void *key1,
                                       const void *key2);

extern void _dictStringCopyHTKeyDestructor(void *privdata, void *key);

dictType dictDomainMessageId = {
        _dictStringCopyHTHashFunction,        /* hash function */
        _dictStringCopyHTKeyDup,              /* key dup */
        NULL,                                 /* val dup */
        _dictStringCopyHTKeyCompare,          /* key compare */
        _dictStringCopyHTKeyDestructor,       /* key destructor */
        domainMessageIdFreeFun  /* val destructor */
};

static void save() {
    CATCS_ENTER(g_id_lock);

    FILE *file = fopen(g_index_filePath, "w");
    if (file == NULL) {
        CATCS_LEAVE(g_id_lock);
        return;
    }

    fprintf(file, "%lld %d", g_last_hour, g_id_index);
    fclose(file);

    CATCS_LEAVE(g_id_lock);
}

static void load() {
    FILE *file = fopen(g_index_filePath, "r");
    if (file == NULL) return;

    fscanf(file, "%lld %d", &g_last_hour, &g_id_index);
    fclose(file);
}

void initMessageIdHelper() {

    if (g_config.enableMultiprocessing) {
        pid_t pid = getpid();
        char tmpBuf[32];
        g_multiprocessing_pid_str = catsdsnew(catItoA(pid, tmpBuf, 10));
    }

    g_index_filePath = catsdsnewEmpty(256);
    g_index_filePath = catsdscatsds(g_index_filePath, g_config.dataDir);
    g_index_filePath = catsdscatsds(g_index_filePath, g_config.indexFileName);
    g_id_lock = CATCreateCriticalSection();
    load();
    unsigned long long nowT = GetTime64();
    unsigned long long nowHour = catTrimToHour(nowT);

    if (nowHour > g_last_hour) {
        g_last_hour = nowHour;
        g_id_index = 0;
        save();
    } else {
        g_id_index += 10000;
        save();
    }
    g_id_prefix = catsdsnewEmpty(256);

    if (g_multiprocessing_pid_str == NULL) {
        g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s-%lld-", g_cat_messageManager.domain,
                                      g_cat_messageManager.ipHex, g_last_hour);
    } else {
        g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s.%s-%lld-", g_cat_messageManager.domain,
                                      g_cat_messageManager.ipHex, g_multiprocessing_pid_str, g_last_hour);
    }

    g_domainMessageIdDict = createCCHashMap(&dictDomainMessageId, 16, NULL);
}

void destroyMessageIdHelper() {
    flushMessageIdMark();
    catsdsfree(g_index_filePath);
    g_index_filePath = NULL;
    catsdsfree(g_id_prefix);
    g_id_prefix = NULL;

    destroyCCHashMap(g_domainMessageIdDict);
}

void flushMessageIdMark() {
    save();
}

sds getNextMessageId() {
    unsigned long long nowT = GetTime64();
    unsigned long long nowHour = catTrimToHour(nowT);

    if (nowHour > g_last_hour) {
        CATCS_ENTER(g_id_lock);
        g_last_hour = nowHour;
        g_id_index = 0;
        catsdsclear(g_id_prefix);

        if (g_multiprocessing_pid_str == NULL) {
            g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s-%lld-", g_cat_messageManager.domain,
                                          g_cat_messageManager.ipHex, g_last_hour);
        } else {
            g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s.%s-%lld-", g_cat_messageManager.domain,
                                          g_cat_messageManager.ipHex, g_multiprocessing_pid_str, g_last_hour);
        }
        CATCS_LEAVE(g_id_lock);
    }

    ++g_id_index;

    sds msgIdStr = catsdsnewEmpty(128);
    msgIdStr = catsdscpylen(msgIdStr, g_id_prefix, catsdslen(g_id_prefix));
    char tmpBuf[32];

    msgIdStr = catsdscat(msgIdStr, catItoA(g_id_index, tmpBuf, 10));

    return msgIdStr;
}

static void *createRemoteDomainFun(CCHashMap *pCCHM, void *key, void *pParam) {
    ATOMICLONG *count = (ATOMICLONG *) malloc(sizeof(ATOMICLONG));
    catChecktPtr(count);
    memset(count, 0, sizeof(ATOMICLONG));
    return count;
}

sds getNextMessageIdByAppkey(const char *domain) {
    sds _domain = catsdsnew(domain);

    if (catsdscmp(g_cat_messageManager.domain, _domain) == 0) {
        catsdsfree(_domain);
        return getNextMessageId();
    } else {
        catsdsfree(_domain);

        unsigned long long nowT = GetTime64();
        unsigned long long nowHour = catTrimToHour(nowT);

        if (nowHour > g_last_hour) {
            CATCS_ENTER(g_id_lock);
            g_last_hour = nowHour;
            g_id_index = 0;
            catsdsclear(g_id_prefix);

            if (g_multiprocessing_pid_str == NULL) {
                g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s-%lld-", g_cat_messageManager.domain,
                                              g_cat_messageManager.ipHex, g_last_hour);
            } else {
                g_id_prefix = catsdscatprintf(g_id_prefix, "%s-%s.%s-%lld-", g_cat_messageManager.domain,
                                              g_cat_messageManager.ipHex, g_multiprocessing_pid_str, g_last_hour);
            }
            CATCS_LEAVE(g_id_lock);
        }

        ATOMICLONG *count = (ATOMICLONG *) findCCHashMapCreateByFun(g_domainMessageIdDict, domain, createRemoteDomainFun,
                                                                  NULL);
        ATOMICLONG_INC(count);

        sds id_prefix = catsdsnewEmpty(256);;

        if (g_multiprocessing_pid_str == NULL) {
            id_prefix = catsdscatprintf(id_prefix, "%s-%s-%lld-", domain,
                                        g_cat_messageManager.ipHex, g_last_hour);
        } else {
            id_prefix = catsdscatprintf(id_prefix, "%s-%s.%s-%lld-", domain, g_cat_messageManager.ipHex,
                                        g_multiprocessing_pid_str, g_last_hour);
        }

        sds msgIdStr = catsdsnewEmpty(128);
        msgIdStr = catsdscpylen(msgIdStr, id_prefix, catsdslen(id_prefix));
        char tmpBuf[32];

        msgIdStr = catsdscat(msgIdStr, catItoA(*count, tmpBuf, 10));

        return msgIdStr;
    }
}

void saveMark() {
    save();
}

