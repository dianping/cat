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
#ifndef CONCURRENT_HASH_MAP_h__
#define CONCURRENT_HASH_MAP_h__

#include "cat_dict.h"
#include "cat_atomic.h"
#include "cat_mutex.h"

// simple implement of concurrentHashMap

#define CCHASHMAP_OK 0
#define CCHASHMAP_ERR 1

#define CCHASHMAP_MAXSLOT 1024

// hash item
extern dictType dictTypeHeapStringCopyKey;
extern dictType dictTypeHeapStrings;
extern dictType dictTypeHeapStringCopyKeyFreeVal;
typedef struct _CCHashSlot {
    dict *m_dict;
    CATCRITICALSECTION m_lock;
} CCHashSlot;

// hash map
typedef struct _CCHashMap {
    int m_hashSlotCount;
    void *m_privateData;
    dictType m_type;
    ATOMICLONG m_count;
    CCHashSlot m_hashSlot[];
} CCHashMap;


/** Defines Val operate function for CCHashMap find. */
typedef void(*CCHashMapValOptFun)(CCHashMap *pCCHM, void *key, volatile void **ppVal, void *pParam);


/** Defines Val create function for CCHashMap find. */
typedef void *(*CCHashMapCreateValFun)(CCHashMap *pCCHM, void *key, void *pParam);

/**********************************************************************************************//**
 * Creates Concurrent hash map.
 *
 * @param [in]  type        If non-null, the type.
 * @param   slotCount           Number of slots.
 * @param [in]  privDataPtr If non-null, the private data pointer.
 *
 * @return  null if it fails, else the new Concurrent hash map.
 **************************************************************************************************/
CCHashMap *createCCHashMap(dictType *type, int slotCount, void *privDataPtr);

/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key, return null if it fails, else the corresponding value 
 *
 * @param [in]  pCCHM   If non-null, the cchm.
 * @param [in]  key     If non-null, the key.
 *
 * @return  null if it fails, else the corresponding value 
 **************************************************************************************************/
void *findCCHashMap(CCHashMap *pCCHM, void *key);

/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key, return defaultVal if it dos'nt contain this key,
 * else the corresponding value 
 *
 * @param [in]  pCCHM       If non-null, the cchm.
 * @param [in]  key         If non-null, the key.
 * @param [in]  defaultVal  If non-null, the default value.
 *
 * @return  defaultVal if there is no this key, else the corresponding value 
 **************************************************************************************************/
void *findCCHashMapDefault(CCHashMap *pCCHM, void *key, void *defaultVal);

/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key. 
 * If it dos'nt contain this key, then create and insert the key-val pair with val:createVal.
 * Only if pCCHM is invalid, the function will always return the corresponding value.
 *
 * @param [in]  pCCHM       If non-null, the cchm.
 * @param [in]  key         If non-null, the key.
 * @param [in]  createVal   If non-null, the create value.
 *
 * @return  null if it fails, else the found Cc hash map create.
 **************************************************************************************************/
void *findCCHashMapCreate(CCHashMap *pCCHM, void *key, void *createVal);

/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key. If it dos'nt contain this key, then
 * create and insert the key-val pair by calling Fun:createFun. Only if pCCHM is invalid, the function
 * will always return the corresponding value.
 *
 * @param [in]  pCCHM           If non-null, the cchm.
 * @param [in]  key             If non-null, the key.
 * @param [in]  createFun       If non-null, the create value.
 * @param [in]  createParam If non-null, the create parameter.
 *
 * @return  null if it fails, else the found Cc hash map create.
 **************************************************************************************************/
void *findCCHashMapCreateByFun(CCHashMap *pCCHM, void *key, CCHashMapCreateValFun createFun, void *createParam);


/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key, return defaultVal if it dos'nt contain this key,
 * else the corresponding value.
 * If the key is found, then call the optFun with param: pCCHM, key, &val, param.
 *
 * @param [in]  pCCHM   If non-null, the cchm.
 * @param [in]  key     If non-null, the key.
 * @param   optFun          The option fun.
 *
 * @return  null if it fails, else the found Cc hash map and operate.
 **************************************************************************************************/
void *findCCHashMapAndOperate(CCHashMap *pCCHM, void *key, CCHashMapValOptFun optFun, void *param);

/**********************************************************************************************//**
 * Find the corresponding value in CCHashMap with key. If it dos'nt contain this key, then
 * create and insert the key-val pair by calling Fun:createFun. 
 * Then call the optFun with param: pCCHM, key, &val, param.
 * Only if pCCHM is invalid, the function will always return the corresponding value.
 *
 * @param [in]  pCCHM           If non-null, the cchm.
 * @param [in]  key             If non-null, the key.
 * @param [in]  createFun       If non-null, the create value.
 * @param [in]  createParam     If non-null, the create parameter.
 * @param   optFun              The option fun.
 * @param [in,out]  optParam    If non-null, the option parameter.
 *
 * @return  null if it fails, else the found Cc hash map create.
 **************************************************************************************************/
void *
findCCHashMapCreateByFunAndOperate(CCHashMap *pCCHM, void *key, CCHashMapCreateValFun createFun, void *createParam,
                                   CCHashMapValOptFun optFun, void *optParam);

/**********************************************************************************************//**
 * put the key-val pair in CCHashMap, if the key exist, return fail
 *
 * @param [in]  pCCHM   If non-null, the cchm.
 * @param [in]  key     If non-null, the key.
 * @param [in]  pVal    If non-null, the value.
 *
 * @return  An int.
 **************************************************************************************************/
int putCCHashMap(CCHashMap *pCCHM, void *key, void *pVal);

/**********************************************************************************************//**
 * replace the key-val pair in CCHashMap, only if pCCHM is invalid, the function will always return success
 *
 * @param [in]  pCCHM   If non-null, the cchm.
 * @param [in]  key     If non-null, the key.
 * @param [in]  pVal    If non-null, the value.
 *
 * @return  An int.
 **************************************************************************************************/
int replaceCCHashMap(CCHashMap *pCCHM, void *key, void *pVal);

/**********************************************************************************************//**
 * Removes the key in CCHashMap.
 *
 * @param [in]  pCCHM   If non-null, the cchm.
 * @param [in]  key     If non-null, the key.
 *
 * @return  An int.
 **************************************************************************************************/
int removeCCHashMap(CCHashMap *pCCHM, void *key);

/**********************************************************************************************//**
 * Clears the Cc hash map described by pCCHM.
 *
 * @param [in,out]  pCCHM   If non-null, the cchm.
 **************************************************************************************************/
void clearCCHashMap(CCHashMap *pCCHM);

/**********************************************************************************************//**
 * Move all dict in CCHashMap to a dict pointer array, the dict * is NULL if there is no more dict
 * User need to call freeDictArray to free the array 
 *
 * @param [in,out]  pCCHM   If non-null, the cchm.
 *
 * @return  null if it fails, else a dict**.
 **************************************************************************************************/
dict **moveCCHashMap(CCHashMap *pCCHM);

/**********************************************************************************************//**
 * Free dict pointer array.
 *
 * @param [in,out]  ppDict  If non-null, the dictionary.
 **************************************************************************************************/
void freeDictArray(dict **ppDict);

void optEveryCCHashMapItem(CCHashMap *pCCHM, CCHashMapValOptFun optFun, void *optParam);

void destroyCCHashMap(CCHashMap *pCCHM);

#ifdef __cplusplus
}
#endif

#endif//CONCURRENT_HASH_MAP_h__
