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
#ifndef CCAT_CAT_ATOMIC_H
#define CCAT_CAT_ATOMIC_H

#ifdef WIN32

#define ATOMICLONG volatile long

#define ATOMICLONG_INC(pAtomicInt) InterlockedIncrement(pAtomicInt)
#define ATOMICLONG_DEC(pAtomicInt) InterlockedDecrement(pAtomicInt)
#define ATOMICLONG_ADD(pAtomicInt, addVal) InterlockedAdd(pAtomicInt, addVal)
#define ATOMICLONG_FAA(pAtomicInt, addVal) InterlockedExchangeAdd(pAtomicInt, addVal)
#define ATOMICLONG_CAS(pAtomicInt, expect, update) InterlockedCompareExchange(pAtomicInt, update, expect)

#elif defined(__linux) || defined(__APPLE__)

#define ATOMICLONG volatile long

#define ATOMICLONG_INC(pAtomicInt) __sync_add_and_fetch(pAtomicInt, 1)
#define ATOMICLONG_DEC(pAtomicInt) __sync_add_and_fetch(pAtomicInt, -1)
#define ATOMICLONG_ADD(pAtomicInt, addVal) __sync_add_and_fetch(pAtomicInt, addVal)
#define ATOMICLONG_FAA(pAtomicInt, addVal) __sync_fetch_and_add(pAtomicInt, addVal)
#define ATOMICLONG_CAS(pAtomicInt, expect, update) __sync_bool_compare_and_swap(pAtomicInt, expect, update)

#endif

#endif //CCAT_CAT_ATOMIC_H
