# CAT 实时监控平台

CAT 是美团点评开源的实时应用监控平台，提供了 `Tracsaction`、`Event`、`Problem`、`Business` 等丰富的指标项。在实际的生产需求中，笔者进行了部分扩展：
1. 链路跟踪：通过 TraceId 搜索消息树，定位问题更高效。
2. 高度集成：客户端引入组件后，只需要开启配置，就完成了 `HTTP`、`Dubbo`、`Redis`、`SQL`、`Log4j2` 埋点。
3. 界面调整：术语汉化、开放彩蛋、LOGO和消息树美化（陆续优化中）

## 服务端概览

### 改造前

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/overview-old.png)

### 改造后

#### Tracing

可以通过 TraceId 查找整个链路的 HTTP 请求耗时、RPC 调用情况、Log4j2 业务日志、SQL 和缓存执行耗时。

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/tracing.png)

#### Transaction

术语汉化，微调字体

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/transaction.png)

#### Event

术语汉化，微调字体

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/event.png)

#### 业务指标

原名为 Business，相对于 Transaction 和 Event 更宏观的指标，需要业务自己埋点。

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/business.png)

推荐使用  [`eden-cat-spring-boot-starter`](https://github.com/shiyindaxiaojie/eden-architect/tree/main/eden-components/eden-spring-integration/src/main/java/org/ylzl/eden/spring/integration/cat) 提供的 `@CatMetric` 注解实现埋点，支持 SpEL 表达式，代码示例如下：

```java
@CatMetric(name = "'客户[' + #cust.custId + ']资产查询调用次数'", count = 1)
public Response listAsset(Cust cust) {
    //
}
```

#### 接口统计

原名为 Matrix，统计所有接口的性能情况

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/matrix.png)

#### 方法调用

可以搜索某个 RPC 接口被调用的情况

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/cross.png)

#### JVM

原名是 Heart Beat，笔者更倾向于 JVM 的叫法

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/jvm.png)

#### 调用拓扑

相对 Zipkin 较为简陋，后续有空可以优化下

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/dependency.png)

#### 服务状态

查看当前 CAT 和应用节点的状态

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/eden-images/cat/state.png)

## 客户端集成

为了减少客户端集成的工作，您可以使用 [eden-architect](https://github.com/shiyindaxiaojie/eden-architect) 框架，只需要两步就可以完成 CAT 的集成。

1. 引入 CAT 依赖
````xml
<dependency>
    <groupId>org.ylzl</groupId>
    <artifactId>eden-cat-spring-boot-starter</artifactId>
</dependency>
````
2. 开启 CAT 配置
````yaml
cat:
  enabled: false # 默认关闭，请按需开启
  trace-mode: true # 开启访问观测
  support-out-trace-id: false # 允许异构子系统间透传链路ID
  home: /tmp
  servers: localhost # CAT 地址
  tcp-port: 2280
  http-port: 8080
````

另外，笔者提供了两种不同应用架构的示例，里面有集成 CAT 的示例。
* 面向领域模型的 **COLA 架构**，代码实例可以查看 [eden-demo-cola](https://github.com/shiyindaxiaojie/eden-demo-cola)
* 面向数据模型的 **分层架构**，代码实例请查看 [eden-demo-layer](https://github.com/shiyindaxiaojie/eden-demo-layer)

## 变更日志

请查阅 [CHANGELOG.md](https://github.com/shiyindaxiaojie/cat/blob/3.1.x/CHANGELOG.md)