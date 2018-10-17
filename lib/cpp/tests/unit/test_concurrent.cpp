//
// Created by Terence on 2018/8/14.
//

#include <gtest/gtest.h>

#include "lib/cat_atomic.h"
#include "lib/cat_mutex.h"
#include "lib/cat_semaphore.h"

TEST(Atomic, test) {
    ATOMICLONG a = 0;

    ASSERT_EQ(1, ATOMICLONG_INC(&a));
    ASSERT_EQ(0, ATOMICLONG_DEC(&a));
    ASSERT_EQ(2, ATOMICLONG_ADD(&a, 2));
    ASSERT_EQ(2, ATOMICLONG_FAA(&a, 2));
    ASSERT_EQ(true, ATOMICLONG_CAS(&a, 4, 5));
    ASSERT_EQ(false, ATOMICLONG_CAS(&a, 4, 5));
}

TEST(Mutex, test) {
    MUTEX mx;
    MUTEX_INIT(mx);
    MUTEX_LOCK(mx);
    MUTEX_UNLOCK(mx);
    MUTEX_DESTROY(mx);
}

TEST(Mutex, testCriticalSection) {
    CATCRITICALSECTION cs = CATCreateCriticalSection();
    CATCS_ENTER(cs);
    CATCS_LEAVE(cs);
    CATDeleteCriticalSection(cs);
}

TEST(Semaphore, test) {
    SEMA s;
    SEMA_INIT(s, 1, 3);
    SEMA_WAIT(s);
    SEMA_POST(s);
    SEMA_WAIT(s);
    SEMA_DESTROY(s);
}

TEST(Semaphore, testWaitTime) {
    SEMA s;
    SEMA_INIT(s, 1, 3);
    long r;
    r = SEMA_WAIT_TIME(s, 100);
    ASSERT_EQ(r, SEMA_WAIT_OK);
    r = SEMA_WAIT_TIME(s, 100);
    ASSERT_NE(r, SEMA_WAIT_OK);
    SEMA_DESTROY(s);
}

TEST(Semaphore, testTryWait) {
    SEMA s;
    SEMA_INIT(s, 1, 3);
    long r;
    r = SEMA_TRYWAIT(s);
    ASSERT_EQ(r, SEMA_WAIT_OK);
    r = SEMA_TRYWAIT(s);
    ASSERT_NE(r, SEMA_WAIT_OK);
    SEMA_DESTROY(s);
}
