# Cat Client for Go

`gocat` 支持 Go 1.8 及以上版本。

`gocat` 高度依赖 `ccat`（基于 CGO 实现）

鉴于我们在 `ccat` 中使用**线程空间**来存储 transaction 栈，并用于构建消息树，我们很难让它和 goroutine 共同工作。（因为 MPG 模型的关系，一个 goroutine 可能会在不同的线程中运行）

因此我们这个版本不支持消息树。但不要担心，我们仍然在尝试解决这个问题，并已经有了一些想法等待实现。

## Installation

### via go get

```bash
$ go get github.com/dianping/cat/lib/go/...
```

## Initialization

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `gocat` 之前完成。

然后你就可以通过下面的代码初始化 `gocat` 了：

```c
import (
    "gocat"
)

func init() {
    gocat.Init("appkey")
}
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

## Documentation

### Transaction

```go
t := cat.NewTransaction(TTYPE, "test")
defer t.Complete()
```

我们强烈建议使用 `defer` 关键字以避免忘记完成 transasction 导致出现问题。

#### Transaction apis

我们提供了一组 API 来修改 transaction 的属性。

* AddData
* SetStatus
* SetDuration
* SetDurationStart
* SetTimestamp
* Complete

这些 API 可以很方便的通过如下代码使用：

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

在使用 Transaction 提供的 API 时，你可能需要注意以下几点：

1. 你可以调用 `AddData` 多次，他们会被 `&` 连接起来。
2. 同时指定 `duration` 和 `durationStart` 是没有意义的，尽管我们在样例中这样做了。
3. 不要忘记完成 transaction！否则你会得到一个毁坏的消息树以及内存泄漏！

#### NewCompletedTransactionWithDuration

记录一个给定耗时（纳秒）的 transaction 并完成它。

鉴于 transaction 已经被自动完成了，`timestamp` 会被拨回（就像它是在过去发生的一样！）

> 注意我们只会在给定的耗时小于 60 秒时才会拨回 `timestamp`

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

记录一个带堆栈信息的错误

错误是一种特殊的 event，默认情况下 `type = Exception` 且 `name = error`。

`name` 可以通过第二个参数来复写。

错误堆栈会被收集并存放在 `data` 属性中

```go
err := errors.New("error")
// With default name 'error'
cat.LogError(err)
// Or you can specify the name through the 2nd parameter.
cat.LogError(err, 'error-name')
```

### Metric

我们每秒钟会聚合 metric 消息。

举例来说，如果你在同一秒种调用了三次（使用相同的 name），我们会对这些值求和并且一次性上报给服务端。

对于 `duration`，我们使用平均值来代替求和。

#### LogMetricForCount

```go
cat.LogMetricForCount("metric-1")
cat.LogMetricForCount("metric-2", 3)
```

#### LogMetricForDurationMs
```go
cat.LogMetricForDuration("metric-3", 150 * time.Millisecond.Nanoseconds())
```
