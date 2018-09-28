# Cat Client Java

[中文文档](./doc/zh-CN.md)

The java cat supports JDK 1.6+

## Installation

### via maven

```xml
<dependency>
    <groupId>com.dianping.cat</groupId>
    <artifactId>cat-client</artifactId>
    <version>${cat.version}</version>
</dependency>
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

> Don't forget to change the `<cat server IP address>` to your own after you copy and paste the contents.

With all the preparations have done, you must configure the project under your module. The unified project name is placed in the directory specified by the module as a file. 

You have to create `src/main/resources/META-INF/app.properties` file, add the following:

```
app.name={appkey}
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) is allowed in appkey.

## Quickstart

```
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

## Api List

### Transaction

log the execution of a transaction

> To avoid of forgetting to complete the Transaction, it's better to surround the Transaction by a try-catch-finally block.

```
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

we offered a list of APIs to modify the Transaction.

* setDurationInMillis
* setTimestamp
* addData

These APIs can be easily used like the following codes.

```
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

There are something you have to know about the Transaction APIs:

1. `Duration` will be calculated while you complete the Transaction. (currentTimestamp - durationStart)
2. Although `durationStart` is as same as `timestamp` by default, you can overwrite it.
3. `durationStart` and `timestamp` are different, the first one represents the start time of the Transaction, and the second one only means created time of a message. (Transaction is just a kind of message)
4. `durationStart` won't work when you specified the `duration`.
5. You can call `addData` several times, the added data will be connected by `&`.

### Event

#### Cat.logEvent

log the occurrence of an event.

```
# Log a event with success status and empty data.
Cat.logEvent("URL.Server", "serverIp");

# Log a event with success status and additional data.
Cat.logEvent("URL.Server", "serverIp", Event.SUCCESS, "ip=${serverIp}");
```
#### Cat.logError

log exceptions that occur in the application.

```
# Error traces will be collected when you use it in an except block.
try {
    1 / 0;
} catch (Exception e) {
    Cat.logError(e);
}

# customize your own error traces through the 1nd parameter which is optional.
Cat.logError("this is my error stack info", e)
```

### Metric

log the sum or average of the business indicators

```
# Counter
Cat.logMetricForCount("metric.key");
Cat.logMetricForCount("metric.key", 3);

# Duration
Cat.logMetricForDuration("metric.key", 5);
```

We do aggregate every seconds.

For example, if you called count 3 times in one second (with the same name), we will just summarised the value of them and reported once to the server.

In case of `duration`, we use `averaged` value instead of `summarised` value.

## Integration

### Log component integration

[log4j](./../../integration/log4j/README.md)
[log4j2](./../../integration/log4j2/README.md)
[logback](./../../integration/logback/README.md)

### URL monitoring integration

[URL monitoring integration with web.xml](./../../integration/URL/README.md)
[URL monitoring integration with springboot](./../../integration/spring-boot/README.md)

### More integration solutions

more integration solutions refer to the content in the root integration folder.

