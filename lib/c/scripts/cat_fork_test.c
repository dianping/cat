#include <sys/socket.h>

#include "client.h"

#include "lib/cat_ezxml.h"
#include "lib/cat_time_util.h"

void foobar(const char *s) {
    for (int i = 0; i < 100; i++) {
        CatTransaction* trans = newTransaction(s, "bar");
        logEvent(s, "bar", "", "");
        trans->complete(trans);
    }
}

/**
 * Do initialize both in master and child process.
 */
void test1() {
    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableDebugLog = 1;
    catClientInitWithConfig("ccat", &config);

    if (fork() == 0) {
        catClientInit("ccat-fork");
        foobar("foo1");
    } else {
        foobar("foo");
    }
}

/**
 * Using auto initialize config.
 */
void test2() {
    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableDebugLog = 1;
    config.enableAutoInitialize = 1;
    catClientInitWithConfig("ccat", &config);

    if (fork() == 0) {
        foobar("foo1");
    } else {
        foobar("foo");
    }
}

/**
 * Initialize cat in master and child process separately.
 */
void test3() {
    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableDebugLog = 1;

    if (fork() == 0) {
        catClientInitWithConfig("ccat-fork", &config);
        foobar("foo1");
    } else {
        catClientInitWithConfig("ccat", &config);
        foobar("foo");
    }
}

int main(int argc, char **argv) {
    test3();
    Sleep(5000);
}
