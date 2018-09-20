//
// Created by Terence on 2018/9/6.
//

#ifndef CCAT_CAT_MPSC_QUEUE_H
#define CCAT_CAT_MPSC_QUEUE_H

#include "cat_atomic.h"
#include "cat_condition.h"
#include "cat_sds.h"
#include "cat_semaphore.h"

typedef struct _queue {
    sds name;
} CatMPSCQueue;

typedef struct _queueInner {
    CatCondition cond_not_empty;
    CatCondition cond_not_full;

    int capacity;
    int mask;

    volatile long head;
    ATOMICLONG tail;
    ATOMICLONG tail_ptr;

    void *arr[];
} CatMPSCQueueInner;

static inline int upper_power_of_2(int n) {
    n--;
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return ++n;
}

#ifdef __cplusplus
extern "C" {
#endif

CatMPSCQueue *newCatMPSCQueue(const char *name, int capacity);

CatMPSCQueue *deleteCatMPSCQueue(CatMPSCQueue *q);

int CatMPSC_offer(CatMPSCQueue *q, void *data);

void *CatMPSC_poll(CatMPSCQueue *q);

int CatMPSC_boffer(CatMPSCQueue *q, void *data, int ms);

void *CatMPSC_bpoll(CatMPSCQueue *q, int ms);

int CatMPSC_size(CatMPSCQueue *q);

int CatMPSC_capacity(CatMPSCQueue *q);

#ifdef __cplusplus
}
#endif

#endif //CCAT_CAT_MPSC_QUEUE_H
