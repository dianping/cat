# Cat Client for Go

Gocat supports Go 1.8+

Gocat is highly dependent on `ccat`. (through CGO)

As we using the thread local to storage the transaction stack in `ccat`, which is neccessary to build message tree. It's hard for us to let it work approriately with goroutines. (Because a goroutine may run in different threads, due to the MPG model)

So we don't support `message tree` in this version. Don't worry, we are still working on it and have some great ideas at the moment.

## Installation

### via go get

```bash
$ go get github.com/dianping/cat/lib/go/...
```

## Initialization

First of all, you have to create `/data/appdatas/cat` directory, read and write permission is required (0644).`/data/applogs/cat` is also required if you'd like to preserve a debug log, it can be very useful while debugging.

And create a config file `/data/appdatas/cat/client.xml` with the following contents.

```xml
<?xml version="1.0" encoding="utf-8"?>
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
    <servers>
        <server ip="<cat server ip address>" port="2280" http-port="8080" />
    </servers>
</config>
```

Don't forget to change the `<cat server IP address>` to your own after you copy and paste the contents.

And then you can initialize gocat with following codes:

```c
import (
    "gocat"
)

func init() {
    gocat.Init("appkey")
}
```

> Only English characters, numbers, underscore and dash is allowed in appkey.

## Documentation

### Transaction

```go
t := cat.NewTransaction(TTYPE, "test")
defer t.Complete()
```

We stongly recommend using `defer` keyword to make sure the transaction be completed, or it may cause problems. 

#### Transaction apis

We offered a list of APIs to modify the transaction.

* AddData
* SetStatus
* SetDuration
* SetTimestamp
* Complete

These APIs can be easily used like the following codes.

```go
t := cat.NewTransaction(TTYPE, "test")
defer t.Complete()
t.AddData("testcase")
t.AddData("foo", "bar")
t.SetStatus(gocat.FAIL)
t.SetTimestamp(time.Now().UnixNano()/1000/1000 - 20*1000)
t.SetDurationInMillis(15 * 1000)
```

There are something you have to know about the transaction apis:

2. You can call `AddData` several times, the added data will be connected by `&`.
3. `Timestamp` represents when the transaction has been created, set timestamp **will not** influence the duration.
4. Due to you can't modify when the transaction begins (DurationStart), `SetDuration` would be a good choice.

#### NewCompletedTransactionWithDuration

Log a transaction with a specified duration in milliseconds and complete it immediately.

Due to the transaction has been auto-completed, the `timestamp` will be turned back. (Like it had been created in the past)

> Note that the specified duration should be less than 60,000 milliseconds, or we won't turn back the timestamp.

```go
cat.NewCompletedTransactionWithDuration(TTYPE, "completed", 24000)
// The following code is illegal
cat.NewCompletedTransactionWithDuration(TTYPE, "completed-over-60s", 65000)
```


### Event

#### LogEvent
```go
// Log a event with success status and empty data.
cat.LogEvent("Event", "E1")

// The third parameter (status) is optional, default by "0".
// It can be any of string value.
// The event will be treated as "problem" unless the given status == cat.CAT_CUSSESS ("0")
// which will be recorded in our problem report.
cat.LogEvent("Event", "E2", gocat.FAIL)
cat.LogEvent("Event", "E3", "failed")

// The fourth parameter (data) is optional, default by "".
// It can be any of string value.
cat.LogEvent("Event", "E4", "failed", "some debug info")

```
#### LogError

Log an error with error stacktrace.

Error is a special event, `type = Exception` and `name = error` by default.

`data` will be collected and set to `error stacktrace`.

```go
err := errors.New("error")
// With default name as 'error'
cat.LogError(err)
// Or you can specify the name
cat.LogError(err, 'error-name')
```

### Metric

We do aggregate every seconds.

For example, if you called count 3 times in one second (with the same name), we will just summarised the value of them and reported once to the server.

In case of `duration`, we use `averaged` value instead of `summarised` value.

#### LogMetricForCount

```go
cat.LogMetricForCount("metric-1")
cat.LogMetricForCount("metric-2", 3)
```

#### LogMetricForDurationMs
```go
cat.LogMetricForDurationMs("metric-3", 150)
```
