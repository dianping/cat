# Cat Client for Java

[中文文档](./README.zh-CN.md)

The java cat client supports JDK 1.6+

## Installation

### via maven

```xml
<dependency>
    <groupId>com.dianping.cat</groupId>
    <artifactId>cat-client</artifactId>
    <version>${cat.version}</version>
</dependency>
```

### via jar

If you don't use maven to manage dependencies, you can directly copy jar/cat-client-3.0.0.jar to the WEB_INF/lib path of your project.

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `cat-client`.

Then you have to create the `src/main/resources/META-INF/app.properties` file in your project with the following contents:

```
app.name={appkey}
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) are allowed in appkey.

Since java cat client will be lazily initialized, it's not necessary to initialize it manually.

## Quickstart

```java
Transaction t = Cat.newTransaction("URL", "pageName");

try {
    Cat.logEvent("URL.Server", "serverIp", Event.SUCCESS, "ip=${serverIp}");
    Cat.logMetricForCount("metric.key");
    Cat.logMetricForDuration("metric.key", 5);

    yourBusiness();

    t.setStatus(Transaction.SUCCESS);
} catch (Exception e) {
    t.setStatus(e);
    Cat.logError(e);
} finally {
    t.complete();
}
```

## API List

### Transaction

> To avoid forgetting to complete the Transaction, it's better to surround the Transaction by a try-catch-finally block.

```java
Transaction t = Cat.newTransaction("URL", "pageName");

try {
    yourBusiness();
    t.setStatus(Transaction.SUCCESS);
} catch (Exception e) {
    t.setStatus(e);
    Cat.logError(e);
} finally {
    t.complete();
}
```

We offered a series of APIs to modify the Transaction.

* addData
* setStatus
* setDurationStart
* setDurationInMillis
* setTimestamp
* complete

These APIs can be easily used with the following codes.

```java
Transaction t = Cat.newTransaction("URL", "pageName");

try {
    t.setDurationInMillis(1000);
    t.setTimestamp(System.currentTimeMillis());
    t.setDurationStart(System.currentTimeMillis() - 1000);
    t.addData("content");
    t.setStatus(Transaction.SUCCESS);
} catch (Exception e) {
    t.setStatus(e);
    Cat.logError(e);
} finally {
    t.complete();
}
```

There is something you have to know about the transaction APIs:

1. You can call `addData` several times, the added data will be connected by `&`.
2. It's meaningless to specify `duration` and `durationStart` in the same transaction, although we did so in the example :)
3. Never forget to complete the transaction! Or you will get corrupted message trees and memory leaks!

### Event

#### Cat.logEvent

Log an event.

```java
# Log an event with success status and empty data.
Cat.logEvent("URL.Server", "serverIp");

# Log an event with given status and given data.
Cat.logEvent("URL.Server", "serverIp", "failed", "ip=${serverIp}");
```

#### Cat.logError

Log an error with error stack info.

Error is a special event, the type of it depend on the class of the given `Throwable e`.

1. If `e` is an instanceof `Error`, the `type` will be set to `Error`.
2. Else if `e` is an instanceof `RuntimeException`, the `type` will be set to `RuntimeException`.
3. The `type` will be set to `Exception` in the other cases.

`name` will be set to the `e.getClass().getName()` by default.

And the stack info will be built and set to `data`.

```java
try {
    1 / 0;
} catch (Exception e) {
    Cat.logError(e);
}
```

You can append your own error message to the top of the stack info like this:

```java
Cat.logError("error(X) := exception(X)", e);
```

#### Cat.logErrorWithCategory

Though `name` has been set to the classname of the given `throwable e` by default, you can use this API to overwrite it.

```java
Exception e = new Exception("syntax error");
Cat.logErrorWithCategory("custom-category", e);
```

Like `logError`, you can also append your own error message to the top of the stack info.

```java
Exception e = new Exception("syntax error");
Cat.logErrorWithCategory("custom-category", "?- X = Y, Y = 2", e);
```

### Metric

```java
# Counter
Cat.logMetricForCount("metric.key");
Cat.logMetricForCount("metric.key", 3);

# Duration
Cat.logMetricForDuration("metric.key", 5);
```

We do aggregation every second.

For example, if you have called count 3 times in one second (with the same name), we will just summarize the value of them and report once to the server.

In the case of `duration`, we use `averaged` value instead of `summarized` value.

## Integration

### Log component integration

* [log4j](../../integration/log4j/README.md)
* [log4j2](../../integration/log4j2/README.md)
* [logback](../../integration/logback/README.md)

### URL monitoring integration

* [URL monitoring integration with web.xml](../../integration/URL/README.md)
* [URL monitoring integration with springboot](../../integration/spring-boot/README.md)

### Other integration solutions

Please refer to [integration](../../integration) for further information.
