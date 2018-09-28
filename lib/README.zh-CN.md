# Cat Client

## 总览

我们目前支持以下编程语言：

* [Java](./java/README.zh-CN.md)
* [C](./c/README.zh-CN.md)
* [C++](./cpp/README.zh-CN.md)
* [Python](./python/README.zh-CN.md)
* [Go](./go/README.zh-CN.md)
* [Node.js](./node.js/README.zh-CN.md)

以下编程语言在我们的支持计划中：

* PHP
* C# (.net)

## 名词解释

### 消息类型

* Transaction

* Event

* Heartbeat

* Metric

### 消息属性

* type

    表示一种类型的消息，比如 `SQL`，`RPC` 或 `HTTP`。

* name

    表示一个特定的行为，举例来说：

    * 如果 **type** 是 `SQL`, **name** 可以是 `select <?> from user where id = <?>`, 表示一个 SQL 模版。
    * 如果 **type** 是 `RPC`, **name** 可以是 `QueryOrderByUserId(string, int)`, 表示一个 API 的函数签名。
    * 如果 **type** 是 `HTTP`, **name** 可以是 `/api/v8/{int}/orders`, 表示基础 URI。

   > 更详细的信息建议在 **data** 字段中记录，比如 api 的参数

* status

    表示消息的状态。

    当消息的状态不为 "0" 时，会被标记成一个 "problem"。无论消息类型是什么，只要被标记为 "problem"，它所在的消息树就不会被聚合，这也意味着你随时可以获取它完整的日志信息。

* data

    记录一个消息的详细信息

    * 如果 **type** 是 `SQL`, **data** 可以是 `id=75442432`
    * 如果 **type** 是 `RPC`, **data** 可以是 `userType=dianping&userId=9987`
    * 如果 **type** 是 `HTTP`, **data** 可以是 `orderId=75442432`

    在一些情况下，`data` 字段会包含错误堆栈信息（比如代表一个 exception 或者 error）

* timestamp

    代表消息的创建时间，会在消息树中展示。

    从 `1970-01-01 00:00:00` 开始到创建时经过的毫秒数。

#### Transaction 特有的参数

* duration

    表示一个 transaction 花费的时间，以毫秒计算。

    会在 transaction 被完成时计算。

    > duration = currentTimestamp() - durationStart

    你可以通过相关 API 在 transaction 被完成前指定 `duration` 的值，并且跳过计算过程。

* durationStart

    表示 transaction 开始执行的时间。

    区别于 `timestamp`，`durationStart` 只用来计算 transaction 的 `duration`，修改 `durationStart` 不会影响 `timestamp`。
