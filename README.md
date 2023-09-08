<img src="https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/readme/icon.png" align="right" />

[license-apache2.0]:https://www.apache.org/licenses/LICENSE-2.0.html

[github-action]:https://github.com/shiyindaxiaojie/Sentinel/actions

[sonarcloud-dashboard]:https://sonarcloud.io/dashboard?id=shiyindaxiaojie_Sentinel

# CAT 实时监控平台

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/readme/language-java-blue.svg) [![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/readme/license-apache2.0-red.svg)][license-apache2.0] [![](https://github.com/shiyindaxiaojie/cat/actions/workflows/maven-ci.yml/badge.svg?branch=3.1.x)][github-action] [![](https://sonarcloud.io/api/project_badges/measure?project=shiyindaxiaojie_cat&metric=alert_status)][sonarcloud-dashboard]

CAT 是美团点评开源的实时应用监控平台，提供了 `Tracsaction`、`Event`、`Problem`、`Business` 等丰富的指标项。在实际的生产需求中，笔者进行了部分扩展：
1. 链路跟踪：通过 `TraceId` 搜索消息树，定位问题更高效。
2. 告警优化：支持邮件、钉钉、企业微信、飞书机器人推送，无需部署额外资源。
3. 组件扩展：新增应用大盘、数据库大盘、缓存大盘、服务大盘告警。

## 演示图例

### 改造前

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/dashboard-old.png)

### 改造后

#### Dashboard

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/dashboard.png)

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/dashboard-app.png)

#### Tracing

可以通过 TraceId 查找整个链路的 HTTP 请求耗时、RPC 调用情况、Log4j2 业务日志、SQL 和缓存执行耗时。

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/tracing.png)

#### Alert

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/dingtalk.png)

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/mail.png)

#### Transaction

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/transaction.png)

#### Event

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/event.png)

#### Business

相对于 Transaction 和 Event 更宏观的指标，需要业务自己埋点。

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/business.png)

推荐使用  [`eden-cat-spring-boot-starter`](https://github.com/shiyindaxiaojie/eden-architect/tree/main/eden-components/eden-spring-integration/src/main/java/org/ylzl/eden/spring/integration/cat) 提供的 `@CatMetric` 注解实现埋点，支持 SpEL 表达式，代码示例如下：

```java
@CatMetric(name = "'客户[' + #cust.custId + ']资产查询调用次数'", count = 1)
public Response listAsset(Cust cust) {
    //
}
```

#### Matrix

统计所有接口的性能情况

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/matrix.png)

#### Cross

可以搜索某个 RPC 接口被调用的情况

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/cross.png)

#### Heart Beat

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/heartbeat.png)

#### Dependency

相对 Zipkin 较为简陋，后续有空可以优化下

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/dependency.png)

#### State

查看当前 CAT 和应用节点的状态

![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/state.png)

## 如何构建

本项目默认使用 Maven 来构建，最快的使用方式是 `git clone` 到本地。在项目的根目录执行 `mvn install -T 4C` 完成本项目的构建。

## 如何启动

### IDEA 启动

1. 在用户目录创建文件夹 `~/.cat/appdatas/cat`，拷贝本项目的 `docs/config` 到该目录下
2. 修改 `docs/config/datasources.xml` 的数据库连接信息
3. 在上述目标数据源执行 `scripts/cat-init-3.3.0.sql` 初始化 
4. 检查 `cat-home` 模块已正确设置了 Facet
   ![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/idea-cat-home-facet.png)
5. 使用 IDEA 配置 Tomcat 服务器，请注意，多网卡情况下可能会出现 `CAT服务端异常:[127.0.0.1]`，请设置 JVM 启动参数 `host.ip` 指定 IP。
   ![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/idea-tomcat-settings.png)
6. 指定访问入口 Context 为 `/cat`
   ![](https://cdn.jsdelivr.net/gh/shiyindaxiaojie/images/cat/idea-tomcat-deployment.png)
7. 运行 Tomcat 服务器，启动成功后，自动打开 `http://localhost:8080/cat`

### Docker 启动

本项目已发布到 [Docker Hub](https://hub.docker.com/repository/docker/shiyindaxiaojie/cat-home)，请参考以下命令运行。

```bash
docker run -e JAVA_OPTS="-Xmx2g -Xms2g -Xmn1g" -e MYSQL_URL="127.0.0.1" -e MYSQL_PORT="3306" -e MYSQL_SCHEMA="cat" -e MYSQL_USERNAME="" -e MYSQL_PASSWD="" -e SERVER_URL="127.0.0.1" -p 8090:8090 --name=cat-home -d shiyindaxiaojie/cat-home
```

## 如何部署

### Tomcat 部署

拷贝本项目的 `docs/config` 到用户目录 `~/.cat/appdatas/cat` 中，按需调整数据库配置。执行 `mvn clean package` 打包成一个 cat-home.war，部署在目标 Tomcat 的 `webapps` 目录下，启动 Tomcat 即可。

### Docker 部署

在项目根目录执行 `docker build -f docker/Dockerfile cat:{tag} .` 打包为镜像。

### Helm 部署

进入 `helm` 目录，执行 `helm install -n cat cat .` 安装，在 K8s 环境将自动创建 CAT 所需的资源文件。

## 如何接入

为了减少客户端集成的工作，您可以使用 [eden-architect](https://github.com/shiyindaxiaojie/eden-architect) 框架，只需要两步就可以完成 CAT 的集成。

1. 引入 CAT 依赖
````xml
<dependency>
    <groupId>io.github.shiyindaxiaojie</groupId>
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

# 如果您使用 Dubbo 组件，请增加对应的过滤器，确保 CAT 埋点正常工作
dubbo:
  provider:
    filter: cat-tracing
  consumer:
    filter: cat-tracing,cat-consumer
````

另外，笔者提供了两种不同应用架构的示例，里面有集成 CAT 的示例。
* 面向领域模型的 **COLA 架构**，代码实例可以查看 [eden-demo-cola](https://github.com/shiyindaxiaojie/eden-demo-cola)
* 面向数据模型的 **分层架构**，代码实例请查看 [eden-demo-layer](https://github.com/shiyindaxiaojie/eden-demo-layer)

## 版本规范

项目的版本号格式为 `x.y.z` 的形式，其中 x 的数值类型为数字，从 0 开始取值，且不限于 0~9 这个范围。项目处于孵化器阶段时，第一位版本号固定使用 0，即版本号为 `0.x.x` 的格式。

* 孵化版本：0.0.1-SNAPSHOT
* 开发版本：1.0.0-SNAPSHOT
* 发布版本：1.0.0

版本迭代规则：

* 1.0.0 <> 1.0.1：兼容
* 1.0.0 <> 1.1.0：基本兼容
* 1.0.0 <> 2.0.0：不兼容

## 变更日志

请查阅 [CHANGELOG.md](https://github.com/shiyindaxiaojie/cat/blob/3.4.x/CHANGELOG.md)
