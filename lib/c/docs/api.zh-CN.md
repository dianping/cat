# Cat client for C

## 快速起步

```c
#include "client.h"

void test() {
    /**
     * 如果当前的栈为空，一个消息树会被创建
     */
    CatTransaction *t1 = newTransaction("foo", "bar1");

    /**
     * Metric 可以在任何地方记录，并不会被加到消息树中
     */
    logMetricForCount("metric-count", 1);
    logMetricForDuration("metric-duration", 200);

    /**
     * 记录一个给定耗时的 transaction 并立刻完成它。
     */
    newCompletedTransactionWithDuration("foo", "bar2-completed", 1000);

    /**
     * Transaction can be nested.
     * Transaction 可以嵌套，最新的 transaction 会被推到栈顶
     */
    CatTransaction *t3 = newTransaction("foo", "bar3");
    t3->setStatus(t3, CAT_SUCCESS);
    /**
     * 当你完成一个 transaction 的时候，它会被从栈里面弹出，并且 duration 会被计算。
     */
    t3->complete(t3);

    char buf[10];
    for (int k = 0; k < 3; k++) {
        /**
         * 创建一个给定耗时的 transaction
         */
        CatTransaction *t4 = newTransactionWithDuration("foo", "bar4-with-duration", 1000);
        snprintf(buf, 9, "bar%d", k);
        /**
         * 记录一个 event，会被添加到当前 transaction 的儿子列表中
         */
        logEvent("foo", buf, CAT_SUCCESS, NULL);
        t4->setStatus(t4, CAT_SUCCESS);
        t4->complete(t4);
    }

    t1->setStatus(t1, CAT_SUCCESS);
    /**
     * 完成 transaction 并将它从栈里弹出
     * 当最后一个元素被弹出时，消息树会被序列化并发送给服务端
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

### 通用 API

#### catClientInit

使用默认参数初始化 `ccat`。

```c
int catClientInit(const char *appkey);
```

等价于下述代码。

```c
return catClientInitWithConfig(appkey, &DEFAULT_CCAT_CONFIG);
```

采用如下配置：

* `encoderType` = CAT_ENCODER_BINARY.
* `enableHeartbeat` is true.
* `enableSampling` is true.
* `enableMultiprocessing` is false.
* `enableDebugLog` is false.

#### catClientInitWithConfig

有时你会想要自定义 `ccat`。

```c
CatClientConfig config = DEFAULT_CCAT_CONFIG;
config.enableHeartbeat = 0;
config.enableDebugLog = 1;
catClientInitWithConfig("ccat", &config);
```

#### catClientDestroy

禁用 `ccat`，退出**sender**，**monitor**，和**aggregator** 线程。

释放所有已申请的资源。

```c
int catClientDestroy();
```

#### isCatEnabled

表示 `ccat` 是否已经被初始化。

```c
int isCatEnabled();
```

### Transaction

你可以在[消息属性](../../_/zh-CN.md#消息属性)中了解 transaction 的属性信息。

#### newTransaction

创建一个 Transaction

```c
CatTransaction *newTransaction(const char *type, const char *name);
```

由于安全原因，我们隐藏了 transaction 的属性，但我们提供了一系列 API 可供你修改它们。

* addData
* addKV
* setStatus
* setTimestamp
* complete
* addChild
* setDurationInMillis
* setDurationStart

他们用起来很简单，就像这样：

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

这里有一些你可能想要知道的：

1. 你可以调用 `addData` 和 `addKV` 多次，他们会被 `&` 连接起来。
2. 同时指定 `duration` 和 `durationStart` 是没有意义的，尽管我们在样例中这样做了。
3. 不要忘记完成 transaction！否则你会得到一个毁坏的消息树以及内存泄漏！

#### newTransactionWithDuration

创建一个给定耗时（毫秒）的 transaction

鉴于 `duration` 已经被指定了，它不会在 transaction 完成时被重算。

> 注意 transaction 并没有被完成！所以你还需要手动完成它。

```c
CatTransaction *newTransactionWithDuration(const char *type, const char *name, unsigned long long duration);
```

这和下面的代码是等价的：

```
CatTransaction *t = newTransaction("type", "name");
t->setDurationInMillis(t4, duration);
return t;
```

> 强调一遍，不要忘记完成 transaction！

#### newCompletedTransactionWithDuration

记录一个给定耗时（毫秒）的 transaction 并完成它。

鉴于 transaction 已经被自动完成了，`timestamp` 会被拨回。

> 注意我们只会在给定的耗时小于 60,000 毫秒时才会拨回 `timestamp`

```c
int duration = 1000;
newCompletedTransactionWithDuration("type", "name", duration);
```

这和下面的代码是等价的：

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

Event 是一个简化版的 Transaction，没有耗时。

#### logEvent

记录一个 Event。

```c
void logEvent(const char *type, const char *name, const char *status, const char *data);
```

#### logError

记录一个 Error。

```c
void logError(const char *msg, const char *errStr);
```

这和下面的代码是等价的：

```c
logEvent("Exception", msg, CAT_ERROR, errStr);
```

#### newEvent

创建一个 Event。

通常情况下你不需要用这个 API，除非你确实需要。

使用 logEvent / logError 是更好的选择。

```c
CatEvent *newEvent(const char *type, const char *name);
```

### Metric

#### logMetricForCount

```c
void logMetricForCount(const char *name, int quantity);
```

指标会每秒上报一次。

举例来说，如果你在同一秒内调用这个 API 三次（可以在不同的线程，我们使用线程安全的 map 来缓存指标的值），只有聚合后的值（求和）会被上报到服务端。

#### logMetricForDuration

```c
void logMetricForDuration(const char *name, unsigned long long duration);
```

就像 `logMetricForCount` 一样，同一秒上报的指标会被聚合，唯一的区别是这里使用平均值取代求和。

### Heartbeat

#### newHeartBeat

创建一个 Heartbeat。

> 心跳会被 ccat 自动上报，
> 因此大多数情况下你不需要使用这个 API，
> 除非你想覆盖我们的心跳信息。
>
> 当你这么做时，不要忘记禁用我们的内置心跳信息。
