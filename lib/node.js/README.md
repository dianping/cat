# Cat Client for Node.js

`nodecat` supports node v8+.

## Changelog

### 3.1.x

As everybody knows that node.js is an event-driven programming language. It's hard for us to trace messages.

Transactions can be intersected, makes it impossible to know which transaction is the parent of another one.

It caused problems, so we fallback the default mode to **Atomic Mode**, which means all messages will be sent immediately after it has been completed.

As the message tree is useful in some cases, we have introduced a brand new **Thread Mode** in this version.

In this mode, the **first** transaction will be the **root** transaction, all the following transactions and events will become its child nodes. Instead of being sent after themselves have been completed, the entire message tree will be sent after the root transaction (their parent) has been completed.

Here is the example usage.

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

The `nodecat` required `libcatclient.so` to be installed in `LD_LIBRARY_PATH`.

Please refer to [ccat installation](../c/README.md) for further information.

## Installation

### via npm

```bash
npm i @dp-cat/client
```

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `ccat`.

Then you can initialize `nodecat` with the following codes:

```js
var cat = require('@dp-cat/client')

cat.init({
    appkey: 'appkey'
})
```
> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) are allowed in appkey.

## Documentation

### Transaction

```js
let t = cat.newTransaction('foo', 'bar')
setTimeout(() => t.complete(), 3000)
```

#### Transaction apis

We offered a series of APIs to modify the transaction.

* addData
* setStatus
* complete

Here is an exapmle:

```js
let t = cat.newTransaction('foo', 'bar')
t.addData("key", "val")
t.addData("context")
t.setStatus(cat.STATUS.SUCCESS)
setTimeout(() => t.complete(), 3000)
```

> You can call `addData` several times, the added data will be connected by `&`.

### Event

#### logEvent

Log an event.

```js
// Log a event with success status and empty data.
cat.logEvent("Event", "E1")

// The 3rd parameter (status) is optional, default is "0".
// It can be any of string value.
// The event will be treated as "problem" unless the given status == cat.STATUS.SUCCESS ("0")
// which will be recorded in our problem report.
cat.logEvent("Event", "E2", cat.STATUS.FAIL)
cat.logEvent("Event", "E3", "failed")

// The 4th parameter (data) is optional, default is "".
// It can be any of string value.
cat.logEvent("Event", "E4", "failed", "some debug info")

// The 4th parameter (data) can also be an object
// In this case, the object will be dumped into json.
cat.logEvent("Event", "E5", "failed", {a: 1, b: 2})
```

#### logError

Log an error with error stack traces.

Error is a special event, with `type = Exception` and `name` is given by the 1st parameter.

And the error stack traces will be added to `data`, which is always useful in debugging.

```js
cat.logError('ErrorInTransaction', new Error())
```
