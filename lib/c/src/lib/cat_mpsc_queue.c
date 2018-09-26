//
// Created by Terence on 2018/9/6.
//

#include "cat_mpsc_queue.h"

#include "headers.h"

#define CAT_MPSC_QUEUE_GET_INNER(queue) (CatMPSCQueueInner *) (queue + sizeof(CatMPSCQueue))

CatMPSCQueue* newCatMPSCQueue(const char *name, int capacity) {
    capacity = upper_power_of_2(capacity);

    int base = sizeof(CatMPSCQueue) + sizeof(CatMPSCQueueInner);
    CatMPSCQueue *queue = malloc(base + capacity * sizeof(void *));
    queue->name = catsdsnew(name);

    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);

    CatConditionInit(&q->cond_not_empty);
    CatConditionInit(&q->cond_not_full);

    q->capacity = capacity;
    q->mask = capacity - 1;

    q->head = 0;
    q->tail = 0;
    q->tail_ptr = 0;

    return queue;
}

CatMPSCQueue* deleteCatMPSCQueue(CatMPSCQueue *queue) {
    catsdsfree(queue->name);
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    CatConditionDestory(&q->cond_not_empty);
    CatConditionDestory(&q->cond_not_full);
    free(queue);
}

static int _offer(CatMPSCQueueInner *q, void* data) {
    while (1) {
        long tail = q->tail;
        if (q->head > tail - q->capacity) {
            if (ATOMICLONG_CAS(&q->tail_ptr, tail, tail + 1)) {
                q->arr[tail & q->mask] = data;
                ATOMICLONG_INC(&q->tail);
                return 0;
            }
        } else {
            return 1;
        }
    }
}

static void* _poll(CatMPSCQueueInner *q) {
    if (q->tail > q->head) {
        long index = q->head & q->mask;
        void* res = q->arr[index];
        q->arr[index] = NULL;
        q->head++;
        return res;
    } else {
        return NULL;
    }
}

/**
 * Offer
 * @param q
 * @param data
 * @return
 */
int CatMPSC_offer(CatMPSCQueue *queue, void *data) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    int res = _offer(q, data);
    CatConditionSignalAll(&q->cond_not_empty);
    return res;
}

/**
 * Poll
 * @param q
 * @return
 */
void* CatMPSC_poll(CatMPSCQueue *queue) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    void* res = _poll(q);
    CatConditionSignalAll(&q->cond_not_full);
    return res;
}

/**
 * Blocking Offer
 * @param q
 * @param data
 * @param ms
 * @return
 */
int CatMPSC_boffer(CatMPSCQueue *queue, void *data, int ms) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    long remain = ms;
    while (remain > 0) {
        if (_offer(q, data) == 0) {
            CatConditionSignalAll(&q->cond_not_empty);
            return 0;
        }
        remain = CatConditionWait(&q->cond_not_full, remain);
    }
    return 1;
}

/**
 * Blocking Poll
 * @param q
 * @param ms
 * @return
 */
void* CatMPSC_bpoll(CatMPSCQueue *queue, int ms) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    void* res;
    long remain = ms;
    while (remain > 0) {
        if (NULL != (res = _poll(q))) {
            CatConditionSignalAll(&q->cond_not_full);
            return res;
        }
        remain = CatConditionWait(&q->cond_not_empty, remain);
    }
    return NULL;
}

/**
 * Return the number of elements in queue.
 * @param q
 * @return
 */
int CatMPSC_size(CatMPSCQueue *queue) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    return (int) (q->tail - q->head);
}

/**
 * Return the capacity of queue.
 * @param q
 * @return
 */
int CatMPSC_capacity(CatMPSCQueue *queue) {
    CatMPSCQueueInner *q = CAT_MPSC_QUEUE_GET_INNER(queue);
    return q->capacity;
}