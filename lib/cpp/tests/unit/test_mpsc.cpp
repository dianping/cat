//
// Created by Terence on 2018/8/14.
//

#include <gtest/gtest.h>

#include <lib/cat_mpsc_queue.h>

#define UT_MPSC_THREAD_NUM 32
#define UT_MPSC_PER_THREAD 1000
#define UT_MPSC_CAPACITY 1000

TEST(MPSC, test) {
    const char* name = "qname";
    CatMPSCQueue *q = newCatMPSCQueue(name, 13);

    ASSERT_STREQ(q->name, name);
    ASSERT_EQ(CatMPSC_size(q), 0);
    ASSERT_EQ(CatMPSC_capacity(q), 16);

    deleteCatMPSCQueue(q);
}
