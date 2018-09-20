# cat c++ client

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

All the cpp cat client apis are in the `cat` namespace.

### Common apis

#### cat::init

initialize cat client by default configs.

```c
void init(const string& domain);
```

Following config is used by default.

* `binary` encoder
* built-in `heartbeat` enabled
* `sampling` enabled
* `multi process mode` disabled
* `debug log` disabled

You can also customize config. (Like to use text encoder instead of binary encoder)

```cpp
cat::Config c = cat::Config();
c.enableDebugLog = true;
c.encoderType = cat::ENCODER_TEXT;
cat::init("cppcat", c);
```

#### cat::destory

Disable the cat client, shutdown `sender`, `monitor` and `aggregator` threads.

Also release all the resources that have been used.

```cpp
void destroy();
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

#### cat::Transaction

Transaction can be easily created

```cpp
cat::Transaction t("type", "name");
```

Due to the properties of a transaction were mostly private, we offered a list of APIs to help you update them.

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

1. Duration of a transaction will be calculated while you complete the transaction. (currentTimestamp - durationStart)
1. Although `durationStart` is as same as `timestamp`, you can overwrite it.
2. `durationStart` and `timestamp` are different, the first one represents the start time of a transaction, and the second one only means created time of a message. (Transaction is one kind of message)
1. `durationStart` won't work when you specified the `duration`.
2. You can call `addData` several times, the added data will be connected by `&`.

> Don't forget to complete the transaction after you new it! Or you will get corrupted message trees and memory leaks!

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

For example, if you called this API 3 times in one second (can be in different threads, we use a concurrent hash map to cache the value), only the aggregated value (summarized) will be reported to the server.

#### logMetricForDuration

```cpp
void logMetricForDuration(const string& key, unsigned long ms);
```

Like `logMetricForCount`, the values reported in one second will be aggregated, the only difference is we reported `averaged` value instead of `summarized` value.
