# Cat Client for Node.js

`nodecat` 支持 node v8 及以上版本。

## Requirements

`nodecat` 需要 `libcatclient.so` 被安装在 `LD_LIBRARY_PATH` 目录下。

请参阅 [ccat 安装](../c/README.zh-CN.md) 以获取进一步的信息。

## Installation

### via npm

```bash
npm install nodecat
```

## Initialization

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `gocat` 之前完成。

然后你就可以通过下面的代码初始化 `nodecat` 了：

```js
var cat = require('nodecat')

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
