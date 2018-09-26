# cat c client

## Quickstart

```c
#include "ccat/client.h"

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
     * Upon you complete the transaction.
     * The transaction will be popped from the stack and the duration will be set.
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
         * Log a event, append the event to the children of current transaction.
         */
        logEvent("foo", buf, CAT_SUCCESS, NULL);
        t4->setStatus(t4, CAT_SUCCESS);
        t4->complete(t4);
    }

    t1->setStatus(t1, CAT_SUCCESS);
    /**
     * When the last element of stack is popped.
     * A message tree will be generated and sent to server.
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

### Common apis

#### catClientInit

initialize cat client by default configs.

```c
int catClientInit(const char *appkey);
```

This is equivalent to the following codes.

```c
return catClientInitWithConfig(appkey, &DEFAULT_CCAT_CONFIG);
```

* `binary` encoder
* built-in `heartbeat` enabled
* `sampling` enabled
* `multi process mode` disabled
* `debug log` disabled

#### catClientInitWithConfig

You may want to customize the cat client in some cases.

```c
CatClientConfig config = DEFAULT_CCAT_CONFIG;
config.enableHeartbeat = 0;
config.enableDebugLog = 1;
catClientInitWithConfig("ccat", &config);
```

#### catClientDestroy

Disable the cat client, shutdown `sender`, `monitor` and `aggregator` threads.

Also release all the resources that have been used.

```c
int catClientDestroy();
```

#### isCatEnabled

Represent if cat client is initialized.

```c
int isCatEnabled();
```

### Transaction

A message has the following properties.

* `type` usually means a category of event, for example, `SQL`, `RPC`, `HTTP-GET` may be recommended types.
* `name` usually means a specified action.
    * When the type is `SQL`, the name may be `select <?> from user where user_id = <?>`
    * When the type is `RPC`, the name may be `queryOrderByUserId`
    * when the type is `HTTP-GET`, the name may be `/api/v2/order/<int>`
* while `status` is not equal to `CAT_SUCCESS` (which is "0"), the message will be treated as a `problem`, and can be recorded in our problem report.
* `data` is used for storing some useful infomation.
    * When the type is `SQL`, the data may like `user_id=194432&token=a94238`
    * When the type is `RPC`, the data may like `order_id=11314152`
* `timestamp` represents the time when the message has been created, which will be shown on our log view page.

Though transaction is inherited from a message, it also has the foregoing properties.

And there are some transaction-only properties:

* `duration` means the time costs of a transaction.
* `durationStart` means the start time of a transaction.

> Note the `durationStart` may not as same as `timestamp`, they have different meanings.

#### newTransaction

Create a transaction.

```c
CatTransaction *newTransaction(const char *type, const char *name);
```

We hid the properties of a transaction (due to safety reasons), but we offered a list of APIs to help you update them.

* addData
* addKV
* setStatus
* setTimestamp
* complete
* addChild
* setDurationInMillis
* setDurationStart

They can be easily used, for example:

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

1. Duration of a transaction will be calculated while you complete the transaction. (currentTimestamp - durationStart)
1. Although `durationStart` is as same as `timestamp`, you can overwrite it.
2. `durationStart` and `timestamp` are different, the first one represents the start time of a transaction, and the second one only means created time of a message. (Transaction is one kind of message)
1. `durationStart` won't work when you specified the `duration`.
2. You can call `addData` and `addKV` several times, the added data will be connected by `&`.

> Don't forget to complete the transaction after you new it! Or you will get corrupted message trees and memory leaks!

#### newTransactionWithDuration

Create a transaction with a specified duration in milliseconds.

Due to duration has been specified, it won't be overwritten after the transaction has been completed.

> Note that the transaction has not been completed, so you have to complete it manually.

```c
CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long duration);
```

This is equivalent to the following codes:

```
CatTransaction *t = newTransaction("type", "name");
t->setDurationInMillis(t4, duration);
return t;
```

> Don't forget to complete the transaction.

#### newCompletedTransactionWithDuration

Log a transaction with a specified duration in milliseconds.

Due to the transaction has been auto-completed, the `durationStart` and the `timestamp` will be turned back.

> Note that the specified duration should be less than 60,000 milliseconds.
>
> Or we won't turn back the start and created time.

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
    t->setDurationStart(t, GetTime64() - duration);
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

log a count metric.

```c
void logMetricForCount(const char *name, int quantity);
```

The metric will be sent every 1 second.

For example, if you called this API 3 times in one second (can be in different threads, we use a concurrent hash map to cache the value), only the aggregated value (summarized) will be reported to the server.

#### logMetricForDuration

```c
void logMetricForDuration(const char *name, unsigned long long duration);
```

Like `logMetricForCount`, the values reported in one second will be aggregated, the only difference is we reported `averaged` value instead of `summarized` value.

### Heartbeat

#### newHeartBeat

Create a heartbeat.

> Heartbeat is reported by cat client automatically,
> so you don't have to use this API in most cases,
> unless you want to overwrite our heartbeat message.
>
> Don't forget to disable our built-in heartbeat if you do so.
