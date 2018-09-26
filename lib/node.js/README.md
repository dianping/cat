# Cat client for Node.js

`nodecat` supports node v8+.

## Requirements

The `nodecat` required `libcatclient.so` installed in `LD_LIBRARY_PATH`.

Please refer to [ccat installation](../c/README.md) for further information.

## Installation

### via npm

```bash
npm install nodecat
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

After you've done all things above, `nodecat` can be initialized with following codes:

```js
var cat = require('nodecat')

cat.init({
    appkey: 'your-appkey'
})
```
> Only English characters, numbers, underscore and dash is allowed in appkey.

## Documentation

### Transaction

```js
let t = cat.newTransaction('foo', 'bar')
setTimeout(() => t.complete(), 3000)
```

#### Transaction apis

We offered a list of APIs to modify the transaction.

* addData
* setStatus
* complete

> You can call `addData` several times, the added data will be connected by `&`.

Here is an exapmle:

```js
let t = cat.newTransaction('foo', 'bar')
t.addData("key", "val")
t.addData("context")
t.setStatus(cat.STATUS.SUCCESS)
setTimeout(() => t.complete(), 3000)
```

### Event

#### logEvent

Log an event.

```js
// Log a event with success status and empty data.
cat.logEvent("Event", "E1")

// The third parameter (status) is optional, default by "0".
// It can be any of string value.
// The event will be treated as "problem" unless the given status == cat.STATUS.SUCCESS ("0")
// which will be recorded in our problem report.
cat.logEvent("Event", "E2", cat.STATUS.FAIL)
cat.logEvent("Event", "E3", "failed")

// The fourth parameter (data) is optional, default by "".
// It can be any of string value.
cat.logEvent("Event", "E4", "failed", "some debug info")

// The fourth parameter (data) can also be an object
// In this case, the object will be dumped into json.
cat.logEvent("Event", "E5", "failed", {a: 1, b: 2})
```

#### logError

Log an error with error stack.

Error is a special event, with `type = Exception` and `name` is given by the 1st parameter.

And error stacktrace will be added to `data`, which is always useful in debugging.

```js
cat.logError('ErrorInTransaction', new Error())
```