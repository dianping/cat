#include <stdio.h>

#include "client.h"

#include "lib/cat_time_util.h"

#define LOOP_A 100
#define LOOP_B 10

void test() {
    /**
     * A message tree will be created if the current transaction stack is empty.
     */
    CatTransaction *t1 = newTransaction("foo", "bar1");

    /**
     * Metric can be logged anywhere and won't be recorded in the message tree.
     */
    logMetricForCount("metric-count", 1);
    logMetricForDuration("metric-duration", 200);

    /**
     * Log a completed transaction with a specified duration.
     */
    newCompletedTransactionWithDuration("foo", "bar2-completed", 1000);

    /**
     * Transaction can be nested.
     * The new transaction will be pushed into the stack.
     */
    CatTransaction *t3 = newTransaction("foo", "bar3");
    t3->setStatus(t3, CAT_SUCCESS);
    /**
     * Once you complete the transaction.
     * The transaction will be popped from the stack and the duration will be calculated.
     */
    t3->complete(t3);

    char buf[10];
    for (int k = 0; k < 3; k++) {
        /**
         * Create a transaction with a specified duration.
         */
        CatTransaction *t4 = newTransactionWithDuration("foo", "bar4-with-duration", 1000);
        snprintf(buf, 9, "bar%d", k);
        /**
         * Log an event, it will be added to the children list of the current transaction.
         */
        logEvent("foo", buf, CAT_SUCCESS, NULL);
        t4->setStatus(t4, CAT_SUCCESS);
        t4->complete(t4);
    }

    t1->setStatus(t1, CAT_SUCCESS);
    /**
     * Complete the transaction and poped it from the stack.
     * When the last element of the stack is popped.
     * The message tree will be encoded and sent to server.
     */
    t1->complete(t1);
}

void test1() {
    for (int i = 0; i < LOOP_A; i++) {
        CatTransaction *t = newTransaction("Test1", "A");
        t->setStatus(t, CAT_SUCCESS);
        t->setTimestamp(t, GetTime64() - 5000);
        t->setDurationStart(t, GetTime64() - 5000);
        t->setDurationInMillis(t, 4200);
        t->addData(t, "data");
        t->addKV(t, "k1", "v1");
        t->addKV(t, "k2", "v2");
        t->complete(t);
    }
}

void test2() {
    CatTransaction *t1 = newTransaction("Test2", "A");
    t1->complete(t1);
}

void test3() {
    for (int i = 0; i < LOOP_A; i++) {
        for (int j = 0; j < LOOP_B; j++) {
            test();
        }
        Sleep(200);
    }
}

int main(int argc, char **argv) {
    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableHeartbeat = 0;
    config.enableMultiprocessing = 1;
    catClientInitWithConfig("ccat", &config);
    test3();
    Sleep(3000);
    catClientDestroy();
    return 0;
}
