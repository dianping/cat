# Cat Client Java

Cat Java 客户端支持 JDK 1.6+

## 安装

### 通过 Maven 添加依赖

```xml
<dependency>
    <groupId>com.dianping.cat</groupId>
    <artifactId>cat-client</artifactId>
    <version>${cat.version}</version>
</dependency>
```

## 初始化

首先你需要创建 `/data/appdatas/cat` 目录，并拥有读写权限 (0644)。建议同时创建 `/data/applogs/cat` 目录用于存放日志，这将在排查问题时非常有用。

然后创建一个配置文件 `/data/appdatas/cat/client.xml`，内容如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
    <servers>
        <server ip="<cat server ip address>" port="2280" http-port="8080" />
    </servers>
</config>
```

> 在复制粘贴文件内容后，不要忘记把 `<cat server IP address>` 改成你自己的IP！

所有准备工作都完成之后, 你必须在你的项目模块下配置规范的项目名，统一项目名以文件的形式放在模块指定的目录下. 

你需要创建 `src/main/resources/META-INF/app.properties` 文件, 添加如下内容:

```
app.name={appkey}
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

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

记录一段事务的执行过程。

> 为了避免忘记关闭 Transaction, 建议在 finally 代码块中执行 complete。

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

我们提供了一系列 API 来对 Transaction 进行修改。

* setDurationInMillis
* setTimestamp
* addData

这些 API 可以被很方便的使用，参考如下代码：

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

在使用 Transaction 提供的 API 时，你可能需要注意以下几点：

1. `Duration` 在 Transaction 结束的时候会被自动计算（当前时间 - 开始时间）
2. 尽管`timestamp`默认是和`durationStart`相同的，你仍然可以复写它。
3. `durationStart`和`timestamp`是不同的，前者代表 Transaction 的开始时间，后者代表 Message 的创建时间（Transaction 也是一种 Message）。
4. 当你指定了 `duration` 时，`durationStart` 就不起作用了。
5. 你可以调用 `addData` 多次，他们会通过 `&` 连接。

### Event

#### Cat.logEvent

记录某种事件的发生。

```
# Log a event with success status and empty data.
Cat.logEvent("URL.Server", "serverIp");

# Log a event with success status and additional data.
Cat.logEvent("URL.Server", "serverIp", Event.SUCCESS, "ip=${serverIp}");
```
#### Cat.logError

记录应用发生的异常。

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

记录业务指标的统计总和以及平均值。

```
# Counter
Cat.logMetricForCount("metric.key");
Cat.logMetricForCount("metric.key", 3);

# Duration
Cat.logMetricForDuration("metric.key", 5);
```

我们每秒会聚合 metric。

举例来说，如果你在同一秒调用 count 三次（相同的 name），我们会累加他们的值，并且一次性上报给服务端。

在 `duration` 的情况下，我们用平均值来取代累加值。

## 集成

### 日志组件集成

[log4j](./../../integration/log4j/README.md)
[log4j2](./../../integration/log4j2/README.md)
[logback](./../../integration/logback/README.md)

### URL监控集成

[URL monitoring integration with web.xml](./../../integration/URL/README.md)
[URL monitoring integration with springboot](./../../integration/spring-boot/README.md)

### 更多集成方案

更多集成方案参考项目根目录下integration目录。
