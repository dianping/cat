# Cat Client for Node.js

`nodecat` 支持 node v8 及以上版本。

## Changelog

### 3.1.x

众所周知，node.js 是一个事件驱动的编程语言，这使得我们很难追踪消息。

Transaction 可以交错，令我们无法得知谁是谁的父节点。

这造成了一些问题，因此我们将默认模式降级为**原子模式**, 所有的消息在 complete 之后都会被立刻发送。

然而消息树在一些场景下很有用，因此我们在这个版本提供了一种船新的**线程模式**。

在这一模式下，第一个 transaction 将会被作为根结点，随后所有的 transaction 和 event 都会被作为子节点。他们不会在 complete 之后被发送，取而代之的是在根结点 complete 后，整个消息树都会被发送。

这里有个例子：

```js
var cat = require('@dp-cat/client')

cat.init({
    appkey: 'nodecat'
})

cat = new cat.Cat(true)

let a = cat.newTransaction("Context", "A")
let b = cat.newTransaction("Context", "B")
let c = cat.newTransaction("Context", "C")

setTimeout(function() {
    b.complete()
}, 1000)

setTimeout(function() {
    c.complete()
}, 1500)

setTimeout(function() {
    a.complete()
    console.log("a complete")
}, 2000)
```

## Requirements

`nodecat` 需要 `libcatclient.so` 被安装在 `LD_LIBRARY_PATH` 目录下。

请参阅 [ccat 安装](../c/README.zh-CN.md) 以获取进一步的信息。

## Installation

### via npm

```bash
npm i @dp-cat/client
```

## Initialization

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `gocat` 之前完成。

然后你就可以通过下面的代码初始化 `nodecat` 了：

```js
var cat = require('@dp-cat/client')

cat.init({
    appkey: 'your-appkey'
})
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

## Documentation

### Transaction

```js
let t = cat.newTransaction('foo', 'bar')
setTimeout(() => t.complete(), 3000)
```

#### Transaction apis

我们提供了一组 API 来修改 transaction 的属性。

* addData
* setStatus
* complete

这些 API 可以很方便的通过如下代码使用：

```js
let t = cat.newTransaction('foo', 'bar')
t.addData("key", "val")
t.addData("context")
t.setStatus(cat.STATUS.SUCCESS)
setTimeout(() => t.complete(), 3000)
```

> 你可以调用 `addData` 多次，他们会被 `&` 连接起来。

### Event

#### logEvent

```js
// 记录一个信息，默认成功并且没有 data
cat.logEvent("Event", "E1")

// 第三个参数 (status) 是可选的，默认是 "0"
// 可以是任意字符串类型
// 当传入的 status 不是 cat.STATUS.SUCCESS ("0") 时，event 会被作为 "problem"
// 并且会被记录到我们的 problem 报表中
cat.logEvent("Event", "E2", cat.STATUS.FAIL)
cat.logEvent("Event", "E3", "failed")

// 第四个参数 (data) 是可选的，默认是 ""
// 可以是任意字符串类型
cat.logEvent("Event", "E4", "failed", "some debug info")

// 第四个参数 (data) 也可以是一个 Object
// 在这个情况下，Object 会被序列化为 json 格式的字符串。
cat.logEvent("Event", "E5", "failed", {a: 1, b: 2})
```

#### logError

记录一个带堆栈信息的错误

错误是一种特殊的 event，默认情况下 `type = Exception`，`name` 通过第一个参数指定。

错误堆栈会被收集并存放在 `data` 属性中，这通常对调试有很大帮助。

```js
cat.logError('ErrorInTransaction', new Error())
```
