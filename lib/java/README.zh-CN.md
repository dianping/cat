# Cat Client for Java

Cat Java 客户端支持 JDK 1.6 及以上版本

## 安装

### 通过 Maven 添加依赖

```xml
<dependency>
    <groupId>com.dianping.cat</groupId>
    <artifactId>cat-client</artifactId>
    <version>${cat.version}</version>
</dependency>
```

### 直接引入jar包

如果没有使用maven管理依赖，可以直接复制 jar/cat-client-3.0.0.jar 到项目 WEB_INF/lib 路径下。

## 初始化

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `cat client` 之前完成。

然后你需要在你的项目中创建 `src/main/resources/META-INF/app.properties` 文件, 并添加如下内容:

```
app.name={appkey}
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

现在java的cat client会自动懒加载，已经没有必要手动初始化客户端。

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

> 为了避免忘记关闭 Transaction, 建议在 finally 代码块中执行 complete。

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

我们提供了一系列 API 来对 Transaction 进行修改。

* addData
* setStatus
* setDurationStart
* setDurationInMillis
* setTimestamp
* complete

这些 API 使用很方便，参考如下代码：

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

在使用 Transaction API 时，你可能需要注意以下几点：

1. 你可以调用 `addData` 多次，添加的数据会被 `&` 连接起来。
2. 同时指定 `duration` 和 `durationStart` 是没有意义的，尽管我们在样例中这样做了。
3. 不要忘记完成 transaction！否则你会得到一个毁坏的消息树以及内存泄漏！

### Event

#### Cat.logEvent

记录一个事件。

```java
# Log a event with success status and empty data.
Cat.logEvent("URL.Server", "serverIp");

# Log an event with given status and given data.
Cat.logEvent("URL.Server", "serverIp", Event.SUCCESS, "ip=${serverIp}");
```

#### Cat.logError

记录一个带有错误堆栈信息的 Error。

Error 是一种特殊的事件，它的 `type` 取决于传入的 `Throwable e`.

1. 如果 `e` 是一个 `Error`, `type` 会被设置为 `Error`。
2. 如果 `e` 是一个 `RuntimeException`, `type` 会被设置为 `RuntimeException`。
3. 其他情况下，`type` 会被设置为 `Exception`。

同时错误堆栈信息会被收集并写入 `data` 属性中。

```java
try {
    1 / 0;
} catch (Throwable e) {
    Cat.logError(e);
}
```

你可以向错误堆栈顶部添加你自己的错误消息，如下代码所示：

```java
Cat.logError("error(X) := exception(X)", e);
```

#### Cat.logErrorWithCategory

尽管 `name` 默认会被设置为传入的 `Throwable e` 的类名，你仍然可以使用这个 API 来复写它。

```java
Exception e = new Exception("syntax error");
Cat.logErrorWithCategory("custom-category", e);
```

就像 `logError` 一样，你也可以向错误堆栈顶部添加你自己的错误消息：

```java
Cat.logErrorWithCategory("custom-category", "?- X = Y, Y = 2", e);
```

### Metric

记录业务指标的总和或平均值。

```java
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

- [log4j](./../../integration/log4j/README.md)
- [log4j2](./../../integration/log4j2/README.md)
- [logback](./../../integration/logback/README.md)

### URL监控集成

- [URL monitoring integration with web.xml](./../../integration/URL/README.md)
- [URL monitoring integration with springboot](./../../integration/spring-boot/README.md)

### 更多集成方案

更多集成方案，请参考[框架埋点方案集成](../../integration)。
