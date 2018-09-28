# Cat Client for C++

## Quickstart

```cpp
#include <iostream>
#include <unistd.h>

#include "cppcat/client.h"

using namespace std;

unsigned long long GetTime64() {
    return static_cast<unsigned long long int>(std::chrono::system_clock::now().time_since_epoch().count() / 1000);
}

void transaction() {
    cat::Transaction t("foo", "bar");
    t.AddData("foo", "1");
    t.AddData("bar", "2");
    t.AddData("foo is a bar");
    t.SetDurationStart(GetTime64() - 1000);
    t.SetTimestamp(GetTime64() - 1000);
    t.SetDurationInMillis(150);
    t.SetStatus(cat::FAIL);
    t.Complete();
}

void event() {
    cat::Event e("foo", "bar");
    e.AddData("foo", "1");
    e.AddData("bar", "2");
    e.AddData("foo is a bar");
    e.SetStatus(cat::SUCCESS);
    e.Complete();

    cat::logEvent("foo", "bar1");
    cat::logEvent("foo", "bar2", "failed");
    cat::logEvent("foo", "bar3", "failed", "k=v");
}

void metric() {
    cat::logMetricForCount("count");
    cat::logMetricForCount("count", 3);
    cat::logMetricForDuration("duration", 100);
}

int main() {
    cat::Config c = cat::Config();
    c.enableDebugLog = true;
    c.encoderType = cat::ENCODER_TEXT;
    cat::init("cppcat", c);

    for (int i = 0; i < 100; i++) {
        transaction();
        event();
        metric();
        usleep(10000);
    }
    usleep(1000000);
    cat::destroy();
}
```

## API list

所有的 `cppcat` API 都在 `cat` 这个命名空间下。

### Common apis

#### cat::init

使用默认配置初始化 `cppcat`

```c
void init(const string& domain);
```

默认采用如下配置：

* `encoderType` = CAT_ENCODER_BINARY.
* `enableHeartbeat` is true.
* `enableSampling` is true.
* `enableMultiprocessing` is false.
* `enableDebugLog` is false.

你也可以自定义配置（比如使用文本序列化取代二进制序列化）

```cpp
cat::Config c = cat::Config();
c.enableDebugLog = true;
c.encoderType = cat::ENCODER_TEXT;
cat::init("cppcat", c);
```

#### cat::destory

禁用 `cppcat`，退出**sender**，**monitor**，和**aggregator** 线程。

释放所有已申请的资源。

```cpp
void destroy();
```

### Transaction

你可以在[消息属性](../../_/zh-CN.md#消息属性)中了解 transaction 的属性信息。

#### cat::Transaction

Transaction 可以很方便的被创建。

```cpp
cat::Transaction t("type", "name");
```

由于大多数的 transaction 属性都是私有的，我们提供了一系列 API 可供你修改它们。

* AddData
* SetStatus
* SetDurationStart
* SetDurationInMillis
* SetTimestamp
* Complete

他们用起来很简单，就像这样：

```cpp
cat::Transaction t("foo", "bar");
t.AddData("foo", "1");
t.AddData("bar", "2");
t.AddData("foo is a bar");
t.SetDurationStart(GetTime64() - 1000);
t.SetTimestamp(GetTime64() - 1000);
t.SetDurationInMillis(150);
t.SetStatus(cat::FAIL);
t.Complete();
```

这里有一些你可能想要知道的：

1. 你可以调用 `AddData` 多次，他们会被 `&` 连接起来。
2. 同时指定 `duration` 和 `durationStart` 是没有意义的，尽管我们在样例中这样做了。
3. 不要忘记完成 transaction！否则你会得到一个毁坏的消息树以及内存泄漏！

### Event

Event 是一个简化版的 Transaction，没有耗时。

#### cat::logEvent

记录一个 Event。

```cpp
void logEvent(const string& type, const string& name, const string& status = SUCCESS, const string& data = "");
```

### Metric

#### logMetricForCount

```cpp
void logMetricForCount(const string& key, unsigned int count = 1);
```

指标会每秒上报一次。

举例来说，如果你在同一秒内调用这个 API 三次（可以在不同的线程，我们使用线程安全的 map 来缓存指标的值），只有聚合后的值（求和）会被上报到服务端。


#### logMetricForDuration

```cpp
void logMetricForDuration(const string& key, unsigned long ms);
```

就像 `logMetricForCount` 一样，同一秒上报的指标会被聚合，唯一的区别是这里使用平均值取代求和。
