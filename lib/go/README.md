# Cat Client for Go

[中文文档](./README.zh-CN.md)

`gocat` supports Go 1.8+

`gocat` is highly dependent on `ccat`. (through CGO)

Since we are using the thread local to storage the transaction stack in `ccat`, which is necessary to build a `message tree`. It's hard for us to let it work appropriately with goroutines. (Because a goroutine can be run in different threads, due to the MPG model)

We don't support `message tree` in this version, but don't worry, we are still working on it and having some great ideas at the moment.

## Installation

### via go get

```bash
$ go get github.com/dianping/cat/lib/go/...
```

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `gocat`.

Then you can initialize `gocat` with the following codes:

```c
import (
    "gocat"
)

func init() {
    gocat.Init("appkey")
}
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) are allowed in appkey.

## Documentation

### Transaction

```go
t := cat.NewTransaction(TTYPE, "test")
defer t.Complete()
```

We strongly recommend `defer` keyword to make sure that the transaction completed, or it may cause problems.

#### Transaction apis

We offered a series of APIs to modify the transaction.

* AddData
* SetStatus
* SetDuration
* SetDurationStart
* SetTimestamp
* Complete

These APIs can be easily used with the following codes.

```go
t := cat.NewTransaction(TTYPE, "test")
defer t.Complete()
t.AddData("testcase")
t.AddData("foo", "bar")
t.SetStatus(gocat.FAIL)
t.SetDurationStart(time.Now().UnixNano() - time.Second.Nanoseconds() * 5)
t.SetTimestamp(time.Now().UnixNano() - time.Second.Nanoseconds())
t.SetDuration(time.Millisecond.Nanoseconds() * 1000)
```

There is something you have to know about the transaction APIs:

1. You can call `AddData` several times, the added data will be connected by `&`.
2. It's meaningless to specify `duration` and `durationStart` in the same transaction, although we did so in the example :)
3. Never forget to complete the transaction! Or you will get corrupted message trees and memory leaks!

#### NewCompletedTransactionWithDuration

Log a transaction with a specified duration in nanoseconds and complete it immediately.

Due to the transaction has been auto-completed, the `timestamp` will be turned back. (Like it had been created in the past)

> Note that the specified duration should be less than 60 seconds, or we won't turn back the timestamp.

```go
cat.NewCompletedTransactionWithDuration(TTYPE, "completed", time.Second.Nanoseconds() * 24)
// The following code is illegal
cat.NewCompletedTransactionWithDuration(TTYPE, "completed-over-60s", time.Second.Nanoseconds() * 65)
```


### Event

#### LogEvent
```go
// Log a event with success status and empty data.
cat.LogEvent("Event", "E1")

// The 3rd parameter (status) is optional, default is "0".
// It can be any of string value.
// The event will be treated as "problem" unless the given status == gocat.SUCCESS ("0")
// which will be recorded in our problem report.
cat.LogEvent("Event", "E2", gocat.FAIL)
cat.LogEvent("Event", "E3", "failed")

// The 4th parameter (data) is optional, default is "".
// It can be any of string value.
cat.LogEvent("Event", "E4", "failed", "some debug info")
```

#### LogError

Log an error with error stack traces.

Error is a special event, `type = Exception` and `name = error` by default.

`name` can be overwritten by the 2nd parameter.

`error stack traces` will be collected and added to `data`.

```go
err := errors.New("error")
// With default name 'error'
cat.LogError(err)
// Or you can specify the name through the 2nd parameter.
cat.LogError(err, 'error-name')
```

### Metric

We do aggregation metrics every second.

For example, if you have called count 3 times in the same second (with the same name), we will just summarize the value of them and report once to the server.

In the case of `duration`, we use `averaged` value instead of `summarized` value.

#### LogMetricForCount

```go
cat.LogMetricForCount("metric-1")
cat.LogMetricForCount("metric-2", 3)
```

#### LogMetricForDuration
```go
cat.LogMetricForDuration("metric-3", 150 * time.Millisecond.Nanoseconds())
```
