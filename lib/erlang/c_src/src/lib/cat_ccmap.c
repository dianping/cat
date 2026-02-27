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
#include "cat_ccmap.h"

#include <string.h>

extern unsigned int _dictStringCopyHTHashFunction(const void *key);


static int getCCHashMapSlotIndexByKey(CCHashMap *pCCHM, void *key) {
    return _dictStringCopyHTHashFunction(key) % pCCHM->m_hashSlotCount;
}

CCHashMap *createCCHashMap(dictType *type, int slotCount, void *privDataPtr) {
    if (type == NULL || slotCount <= 0 || slotCount > CCHASHMAP_MAXSLOT) {
        return NULL;
    }
    int hashMapSize = sizeof(CCHashMap) + slotCount * sizeof(CCHashSlot);
    CCHashMap *pHashMap = (CCHashMap *) malloc(hashMapSize);
    if (pHashMap == NULL) {
        return NULL;
    }
    memset(pHashMap, 0, hashMapSize);
    pHashMap->m_hashSlotCount = slotCount;
    pHashMap->m_privateData = privDataPtr;
    memcpy(&pHashMap->m_type, type, sizeof(dictType));
    int i = 0;
    for (; i < slotCount; ++i) {
        pHashMap->m_hashSlot[i].m_lock = CATCreateCriticalSection();
    }
    return pHashMap;
}

void *findCCHashMap(CCHashMap *pCCHM, void *key) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        return NULL;
    }
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
    void *val = pEntry == NULL ? NULL : pEntry->val;
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}

void *findCCHashMapDefault(CCHashMap *pCCHM, void *key, void *defaultVal) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        return defaultVal;
    }
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
    void *val = pEntry == NULL ? defaultVal : pEntry->val;
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}

void *findCCHashMapCreate(CCHashMap *pCCHM, void *key, void *createVal) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
        // check again in lock
        if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
            pCCHM->m_hashSlot[idx].m_dict = catDictCreate(&pCCHM->m_type, NULL);
        }
    }
    int rst = catDictAdd(pCCHM->m_hashSlot[idx].m_dict, key, createVal);
    void *val = createVal;
    // if add fails, it means there is already a key-val pair in pCCHM
    if (rst == DICT_ERR) {
        dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
        val = pEntry == NULL ? NULL : pEntry->val;
    }
    if (rst == DICT_OK) {
        ATOMICLONG_INC(&pCCHM->m_count);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}

void *findCCHashMapAndOperate(CCHashMap *pCCHM, void *key, CCHashMapValOptFun optFun, void *param) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        return NULL;
    }
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
    void *val = pEntry == NULL ? NULL : pEntry->val;
    if (val != NULL) {
        // @todo need check
        optFun(pCCHM, key, &pEntry->val, param);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}


void *findCCHashMapCreateByFun(CCHashMap *pCCHM, void *key, CCHashMapCreateValFun createFun, void *createParam) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        // check again in lock
        if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
            pCCHM->m_hashSlot[idx].m_dict = catDictCreate(&pCCHM->m_type, NULL);
        }
    }
    dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
    void *val = pEntry == NULL ? NULL : pEntry->val;
    if (pEntry == NULL) {
        // @todo need check
        val = createFun(pCCHM, key, createParam);
        catDictAdd(pCCHM->m_hashSlot[idx].m_dict, key, val);
        ATOMICLONG_INC(&pCCHM->m_count);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}

void *
findCCHashMapCreateByFunAndOperate(CCHashMap *pCCHM, void *key, CCHashMapCreateValFun createFun, void *createParam,
                                   CCHashMapValOptFun optFun, void *optParam) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        // check again in lock
        if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
            pCCHM->m_hashSlot[idx].m_dict = catDictCreate(&pCCHM->m_type, NULL);
        }
    }
    dictEntry *pEntry = catDictFind(pCCHM->m_hashSlot[idx].m_dict, key);
    void *val = pEntry == NULL ? NULL : pEntry->val;
    if (pEntry == NULL) {
        // @todo need check
        val = createFun(pCCHM, key, createParam);
        // @todo need check
        optFun(pCCHM, key, &val, optParam);
        catDictAdd(pCCHM->m_hashSlot[idx].m_dict, key, val);
        ATOMICLONG_INC(&pCCHM->m_count);
    } else {
        // @todo need check
        optFun(pCCHM, key, &pEntry->val, optParam);
    }


    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return val;
}


int putCCHashMap(CCHashMap *pCCHM, void *key, void *pVal) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
            pCCHM->m_hashSlot[idx].m_dict = catDictCreate(&pCCHM->m_type, NULL);
        }
    }
    int rst = catDictAdd(pCCHM->m_hashSlot[idx].m_dict, key, pVal);
    if (rst == DICT_OK) {
        ATOMICLONG_INC(&pCCHM->m_count);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return rst;
}

int replaceCCHashMap(CCHashMap *pCCHM, void *key, void *pVal) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
            pCCHM->m_hashSlot[idx].m_dict = catDictCreate(&pCCHM->m_type, NULL);
        }
    }
    int rst = catDictReplace(pCCHM->m_hashSlot[idx].m_dict, key, pVal);
    // return err means there has contained the key, ok means the key is new
    if (rst == DICT_OK) {
        ATOMICLONG_INC(&pCCHM->m_count);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return CCHASHMAP_OK;
}

int removeCCHashMap(CCHashMap *pCCHM, void *key) {
    int idx = getCCHashMapSlotIndexByKey(pCCHM, key);
    if (pCCHM->m_hashSlot[idx].m_dict == NULL) {
        return CCHASHMAP_ERR;
    }
    CATCS_ENTER(pCCHM->m_hashSlot[idx].m_lock);
    int rst = catDictDelete(pCCHM->m_hashSlot[idx].m_dict, key);
    if (rst == DICT_OK) {
        ATOMICLONG_DEC(&pCCHM->m_count);
    }
    CATCS_LEAVE(pCCHM->m_hashSlot[idx].m_lock);
    return rst;
}


dict **moveCCHashMap(CCHashMap *pCCHM) {
    dict **ppDict = (dict **) malloc(sizeof(dict *) * (pCCHM->m_hashSlotCount + 1));
    if (ppDict == NULL) {
        return NULL;
    }
    memset(ppDict, 0, sizeof(dict *) * (pCCHM->m_hashSlotCount + 1));
    int i = 0;
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        CATCS_ENTER(pCCHM->m_hashSlot[i].m_lock);
    }
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        ppDict[i] = pCCHM->m_hashSlot[i].m_dict;
        pCCHM->m_hashSlot[i].m_dict = NULL;
    }
    pCCHM->m_count = 0;
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        CATCS_LEAVE(pCCHM->m_hashSlot[i].m_lock);
    }
    ppDict[i] = NULL;
    return ppDict;
}

void freeDictArray(dict **ppDict) {
    free(ppDict);
}


void optEveryCCHashMapItem(CCHashMap *pCCHM, CCHashMapValOptFun optFun, void *optParam) {
    int i = 0;
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        CATCS_ENTER(pCCHM->m_hashSlot[i].m_lock);
        if (pCCHM->m_hashSlot[i].m_dict != NULL) {
            dictIterator *iter = catDictGetIterator(pCCHM->m_hashSlot[i].m_dict);
            if (iter != NULL) {
                dictEntry *pEntry = NULL;
                while ((pEntry = catDictNext(iter)) != NULL) {
                    optFun(pCCHM, pEntry->key, &pEntry->val, optParam);
                }
                catDictReleaseIterator(iter);
            }
        }
        CATCS_LEAVE(pCCHM->m_hashSlot[i].m_lock);
    }
}


void clearCCHashMap(CCHashMap *pCCHM) {
    int i = 0;
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        CATCS_ENTER(pCCHM->m_hashSlot[i].m_lock);
    }
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        if (pCCHM->m_hashSlot[i].m_dict != NULL) {
            catDictEmpty(pCCHM->m_hashSlot[i].m_dict);
        }
    }
    pCCHM->m_count = 0;
    for (i = 0; i < pCCHM->m_hashSlotCount; ++i) {
        CATCS_LEAVE(pCCHM->m_hashSlot[i].m_lock);
    }
}

void destroyCCHashMap(CCHashMap *pCCHM) {
    int i = 0;
    for (; i < pCCHM->m_hashSlotCount; ++i) {
        CATDeleteCriticalSection(pCCHM->m_hashSlot[i].m_lock);
        if (pCCHM->m_hashSlot[i].m_dict != NULL) {
            catDictRelease(pCCHM->m_hashSlot[i].m_dict);
        }
    }
    free(pCCHM);
}
