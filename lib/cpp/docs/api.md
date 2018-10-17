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

All the `cppcat` APIs are in the `cat` namespace.

### Common APIs

#### cat::init

initialize `cppcat` by default configs.

```c
void init(const string& domain);
```

Following config is used by default.

* `encoderType` = CAT_ENCODER_BINARY.
* `enableHeartbeat` is true.
* `enableSampling` is true.
* `enableMultiprocessing` is false.
* `enableDebugLog` is false.

You can also customize config. (Like to use text encoder instead of the binary encoder)

```cpp
cat::Config c = cat::Config();
c.enableDebugLog = true;
c.encoderType = cat::ENCODER_TEXT;
cat::init("cppcat", c);
```

#### cat::destory

Disable the cat client, shutdown `sender`, `monitor` and `aggregator` threads.

And then release all the resources that have been used.

```cpp
void destroy();
```

### Transaction

You can find more information about the properties of a transaction in [Message properties](../../README.md#message-properties)

#### cat::Transaction

Transaction can be easily created.

```cpp
cat::Transaction t("type", "name");
```

Due to the properties of a transaction were mostly private, we offered a list of APIs to help you to edit them.

* AddData
* SetStatus
* SetDurationStart
* SetDurationInMillis
* SetTimestamp
* Complete

These can be easily used, for example:

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

There is something you may want to know:

1. You can call `AddData` several times, the added data will be connected by `&`.
2. It's meaningless to specify `duration` and `durationStart` in the same transaction, although we do it in the example :)
3. Never forget to complete the transaction! Or you will get corrupted message trees and memory leaks!

### Event

The event is just a simplified transaction, which has no duration.

#### cat::logEvent

Log an event message.

```cpp
void logEvent(const string& type, const string& name, const string& status = SUCCESS, const string& data = "");
```

### Metric

#### logMetricForCount

Log a count metric.

```cpp
void logMetricForCount(const string& key, unsigned int count = 1);
```

The metric will be sent every 1 second.

For example, if you have called this API 3 times in one second (can be in different threads, we use a concurrent hash map to cache the value), only the aggregated value (summarized) will be reported to the server.

#### logMetricForDuration

```cpp
void logMetricForDuration(const string& key, unsigned long ms);
```

Like `logMetricForCount`, the metrics that have been logged in the same second will be aggregated, the only difference is `averaged` value is used instead of `summarized` value.
