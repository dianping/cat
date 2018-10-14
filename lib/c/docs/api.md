# Cat Client for C

## Quickstart

```c
#include "client.h"

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

int main(int argc, char **argv) {
    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableHeartbeat = 0;
    config.enableDebugLog = 1;
    catClientInitWithConfig("ccat", &config);
    test();
    Sleep(3000);
    catClientDestroy();
    return 0;
}
```

## API list

### Common APIs

#### catClientInit

initialize `ccat` by default configs.

```c
int catClientInit(const char *appkey);
```

This is equivalent to the following codes.

```c
return catClientInitWithConfig(appkey, &DEFAULT_CCAT_CONFIG);
```

With following configs:

* `encoderType` = CAT_ENCODER_BINARY.
* `enableHeartbeat` is true.
* `enableSampling` is true.
* `enableMultiprocessing` is false.
* `enableDebugLog` is false.

#### catClientInitWithConfig

You may want to customize the `ccat` in some cases.

```c
CatClientConfig config = DEFAULT_CCAT_CONFIG;
config.enableHeartbeat = 0;
config.enableDebugLog = 1;
catClientInitWithConfig("ccat", &config);
```

#### catClientDestroy

Disable the `ccat`, shutdown `sender`, `monitor` and `aggregator` threads.

And then release all the resources that have been used.

```c
int catClientDestroy();
```

#### isCatEnabled

Represent if `ccat` has been initialized.

```c
int isCatEnabled();
```

### Transaction

You can find more information about the properties of a transaction in [Message properties](../../README.md#message-properties)

#### newTransaction

Create a transaction.

```c
CatTransaction *newTransaction(const char *type, const char *name);
```

We hide the properties of a transaction (due to safety reasons), but we offered a list of APIs to help you to edit them.

* addData
* addKV
* setStatus
* setTimestamp
* complete
* addChild
* setDurationInMillis
* setDurationStart

These can be easily used, for example:

```c
CatTransaction *t = newTransaction("Test1", "A");
t->setStatus(t, CAT_SUCCESS);
t->setTimestamp(t, GetTime64() - 5000);
t->setDurationStart(t, GetTime64() - 5000);
t->setDurationInMillis(t, 4200);
t->addData(t, "data");
t->addKV(t, "k1", "v1");
t->addKV(t, "k2", "v2");
t->complete(t);
```

There is something you may want to know:

1. You can call `addData` and `addKV` several times, the added data will be connected by `&`.
2. It's meaningless to specify `duration` and `durationStart` in the same transaction, although we do it in the example :)
3. Never forget to complete the transaction! Or you will get corrupted message trees and memory leaks!

#### newTransactionWithDuration

Create a transaction with a specified duration in milliseconds.

Due to `duration` has been specified, it won't be recalculated after the transaction has been completed.

> Note that the transaction has not been completed! So it is necessary to complete it manually.

```c
CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long duration);
```

This is equivalent to the following codes:

```
CatTransaction *t = newTransaction("type", "name");
t->setDurationInMillis(t4, duration);
return t;
```

> Once again, don't forget to complete the transaction!

#### newCompletedTransactionWithDuration

Log a transaction with a specified duration in milliseconds and complete it.

Due to the transaction has been auto-completed, the `timestamp` will be turned back.

> Note that the specified duration should be less than 60,000 milliseconds.
>
> Or we won't turn back the timestamp.

```c
int duration = 1000;
newCompletedTransactionWithDuration("type", "name", duration);
```

This is equivalent to the following codes:

```c
// return current timestamp in milliseconds.
unsigned long GetTime64();

CatTransaction *t = newTransaction("type", "name");
t->setDurationInMillis(t, duration);
if (duration < 60000) {
    t->setTimestamp(t, GetTime64() - duration);
}
t->complete(t);
return;
```

### Event

The event is just a simplified transaction, which has no duration.

#### logEvent

Log an event message.

```c
void logEvent(const char *type, const char *name, const char *status, const char *data);
```

#### logError

Log an error message.

```c
void logError(const char *msg, const char *errStr);
```

This is equivalent to the following codes:

```c
logEvent("Exception", msg, CAT_ERROR, errStr);
```

#### newEvent

Create an event.

Avoid of using this API unless you really have to do so.

Using logEvent / logError is a better idea.

```c
CatEvent *newEvent(const char *type, const char *name);
```

### Metric

#### logMetricForCount

```c
void logMetricForCount(const char *name, int quantity);
```

The metric will be sent every 1 second.

For example, if you have called this API 3 times in one second (can be in different threads, we use a concurrent hash map to cache the value), only the aggregated value (summarized) will be reported to the server.

#### logMetricForDuration

```c
void logMetricForDuration(const char *name, unsigned long long duration);
```

Like `logMetricForCount`, the metrics that have been logged in the same second will be aggregated, the only difference is `averaged` value is used instead of `summarized` value.

### Heartbeat

#### newHeartBeat

Create a heartbeat.

> Heartbeat is reported by ccat automatically,
> so you don't have to use this API in most cases,
> unless you want to overwrite our heartbeat message.
>
> Don't forget to disable our built-in heartbeat if you do so.
