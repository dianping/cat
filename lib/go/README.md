# Cat Client Golang

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
