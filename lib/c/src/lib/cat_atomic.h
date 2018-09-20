//
// Created by Terence on 2018/8/14.
//

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
