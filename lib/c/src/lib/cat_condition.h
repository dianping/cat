//
// Created by Terence on 2018/9/20.
//

#ifndef CCAT_CAT_CONDITION_H
#define CCAT_CAT_CONDITION_H

#include <pthread.h>
#include <sys/time.h>
#include <stdio.h>

typedef struct _CatCondition {
    pthread_mutex_t mutex;
    pthread_cond_t cond;
} CatCondition;

static inline void CatConditionInit(CatCondition *cond) {
    pthread_condattr_t condattr = {0};
    pthread_cond_init(&cond->cond, &condattr);
    pthread_mutex_init(&cond->mutex, NULL);
}

static inline void CatConditionDestory(CatCondition *cond) {
    pthread_cond_destroy(&cond->cond);
    pthread_mutex_destroy(&cond->mutex);
}

static inline void CatConditionSignalAll(CatCondition *cond) {
    pthread_cond_signal(&cond->cond);
}

/**
 *
 * @param cond
 * @param remain in nanoseconds.
 * @return
 */
static inline long CatConditionWait(CatCondition *cond, long remainInMillis) {
    struct timeval t1, t2;
    gettimeofday(&t1, NULL);
    pthread_mutex_lock(&cond->mutex);
    struct timespec ts = {
            t1.tv_sec + remainInMillis / 1000,
            t1.tv_usec * 1000 + remainInMillis % 1000 * 1000 * 1000
    };
    pthread_cond_timedwait(&cond->cond, &cond->mutex, &ts);

    pthread_mutex_unlock(&cond->mutex);
    gettimeofday(&t2, NULL);

    long delta = (t2.tv_sec - t1.tv_sec) * 1000 + (t2.tv_usec - t1.tv_usec) / 1000;
    return remainInMillis - delta;
}

#endif //CCAT_CAT_CONDITION_H
