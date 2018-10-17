**CAT**
 [![Build Status](https://travis-ci.org/dianping/cat.png?branch=master)](https://travis-ci.org/dianping/cat)
 [![GitHub stars](https://img.shields.io/github/stars/dianping/cat.svg?style=social&label=Star&)](https://github.com/dianping/cat/stargazers)
 [![GitHub forks](https://img.shields.io/github/forks/dianping/cat.svg?style=social&label=Fork&)](https://github.com/dianping/cat/fork)


什么是CAT
===

#### Cat是基于Java开发的实时应用监控平台，为美团点评提供了全面的实时监控告警服务

+ CAT作为服务端项目基础组件，提供了java, c/c++, node, python, go等多语言客户端，已经在美团点评的基础架构中间件框架（MVC框架，RPC框架，数据库框架，缓存框架等，消息队列，配置系统等）深度集成，为美团点评各业务线提供系统丰富的性能指标、健康状况、实时告警等。
+ CAT很大的优势是它是一个实时系统，CAT大部分系统是分钟级统计，但是从数据生成到服务端处理结束是秒级别，秒级定义是48分钟40秒，基本上看到48分钟38秒数据，整体报表的统计粒度是分钟级；第二个优势，监控数据是全量统计，客户端预计算；链路数据是采样计算。

#### Cat的产品价值

- 减少线上问题的发现时间
- 减少问题故障的定位时间
- 辅助应用程序的优化工具

#### Cat的优势

- 实时处理：信息的价值会随时间锐减，尤其是事故处理过程中。
- 全量数据：最开始的设计目标就是全量采集，全量的好处有很多。
- 高可用：所有应用都倒下了，需要监控还站着，并告诉工程师发生了什么，做到故障还原和问题定位。
- 故障容忍：CAT 本身故障不应该影响业务正常运转，CAT 挂了，应用不该受影响，只是监控能力暂时减弱。
- 高吞吐：要想还原真相，需要全方位地监控和度量，必须要有超强的处理吞吐能力。
- 可扩展：支持分布式、跨 IDC 部署，横向扩展的监控系统。

#### CAT支持的监控消息类型包括：

+  **Transaction**	  适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，Transaction用来记录一段代码的执行时间和次数。
+  **Event**	   用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小。
+  **Heartbeat**	表示程序内定期产生的统计信息, 如CPU%, MEM%, 连接池状态, 系统负载等。
+  **Metric**	  用于记录业务指标、指标可能包含对一个指标记录次数、记录平均值、记录总和，业务指标最低统计粒度为1分钟。


内部模型 - 消息树
===

CAT监控系统将每次URL、Service的请求内部执行情况都封装为一个完整的消息树、消息树可能包括Transaction、Event、Heartbeat、Metric等信息。

完整的消息树
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll01.png)
可视化消息树
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll02.png)

分布式消息树【一台机器调用另外一台机器】
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll03.png)


Quick Start
===

### CAT 安装环境

* Linux 2.6以及之上（2.6内核才可以支持epoll），线上服务端部署请使用Linux环境，Mac以及Windows环境可以作为开发环境，美团点评内部CentOS 6.5
* Java  6，7，8，服务端推荐是用jdk7的版本，客户端jdk6、7、8都支持
* Maven 3.3.3
* MySQL 5.6，5.7，更高版本MySQL都不建议使用，不清楚兼容性
* J2EE容器建议使用tomcat，建议版本7.0.70，高版本tomcat默认了get字符串限制，需要修改一些配置才可以生效，不然提交配置可能失败。
* Hadoop环境可选，一般建议规模较小的公司直接使用磁盘模式，可以申请CAT服务端，500GB磁盘或者更大磁盘，这个磁盘挂载在/data/目录上

### Cat目录简介
* cat-doc：服务端报表文档，用markdown写的
* integration：cat和一些第三方工具集成的内容（此部分一部分是由社区贡献，一部分官方贡献）
* lib：cat的客户端，包括Java、C、Python、node、Go
* script：包括了cat的安装的数据库文档

### Cat客户端集成

Cat目前支持：：Java、C、Python、node、Go等语言的接入。详情请参考：[传送门](https://github.com/dianping/cat/tree/master/lib)

* [**Java**](https://github.com/dianping/cat/blob/master/lib/java)
    >  根目录下 cat-client 模块以后不再维护，目前仅做服务端的依赖模块。下个大版本更新计划移除。
* [**C**](https://github.com/dianping/cat/blob/master/lib/c)
* [**C++**](https://github.com/dianping/cat/blob/master/lib/cpp)
* [**Python**](https://github.com/dianping/cat/blob/master/lib/python)
* [**Go**](https://github.com/dianping/cat/blob/master/lib/go)
* [**Node.js**](https://github.com/dianping/cat/blob/master/lib/node.js)


### 安装CAT集群大致步骤

1. 初始化Mysql数据库，一套CAT集群部署一个数据库，初始化脚本在script下的CatApplication.sql
2. 准备三台CAT服务器，IP比如为10.1.1.1，10.1.1.2，10.1.1.3，下面的例子会以这个IP为例子
3. 初始化/data/目录，配置几个配置文件/data/appdatas/cat/*.xml 几个配置文件，具体下面有详细说明
4. 打包cat.war 放入tomcat容器
5. 修改一个路由配置，重启tomcat


### 1、tomcat启动参数调整，修改 catalina.sh文件【服务端】

#### 需要每台CAT集群10.1.1.1，10.1.1.2，10.1.1.3都进行部署
#### 建议使用cms gc策略
#### 建议cat的使用堆大小至少10G以上，开发环境启动2G堆启动即可

```

CATALINA_OPTS="$CATALINA_OPTS -server -Djava.awt.headless=true -Xms25G -Xmx25G -XX:PermSize=256m -XX:MaxPermSize=256m -XX:NewSize=10144m -XX:MaxNewSize=10144m -XX:SurvivorRatio=10 -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:MaxTenuringThreshold=13 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+UseCMSCompactAtFullCollection -XX:+CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=9 -XX:CMSInitiatingOccupancyFraction=60 -XX:+CMSClassUnloadingEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:-ReduceInitialCardMarks -XX:+CMSPermGenSweepingEnabled -XX:CMSInitiatingPermOccupancyFraction=70 -XX:+ExplicitGCInvokesConcurrent -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="%CATALINA_HOME%\conf\logging.properties" -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC -Xloggc:/data/applogs/heap_trace.txt -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/applogs/HeapDumpOnOutOfMemoryError -Djava.util.Arrays.useLegacyMergeSort=true"

```

#### 修改中文乱码 tomcat conf 目录下 server.xml

```
<Connector port="8080" protocol="HTTP/1.1"
           URIEncoding="utf-8"    connectionTimeout="20000"
               redirectPort="8443" />  增加  URIEncoding="utf-8"
                            
```

### 2、程序对于/data/目录具体读写权限【包括客户端&服务端】

- 注意无论是CAT客户端和服务端都要求/data/目录能进行读写操作，如果/data/目录不能写，建议使用linux的软链接链接到一个固定可写的目录，软链接的基本命令请自行搜索google
- 此目录会存一些CAT必要的配置文件，运行时候的缓存文件，建议不要修改，如果想改，请自行研究好源码里面的东西，在酌情修改，此目录不支持进行配置化
- mkdir /data
- chmod 777 /data/ -R

- 如果是Windows开发环境则是对程序运行盘下的/data/appdatas/cat和/data/applogs/cat有读写权限,如果cat服务运行在e盘的tomcat中，则需要对e:/data/appdatas/cat和e:/data/applogs/cat有读写权限
- 如果windows实在不知道哪个盘，就所有盘都建好，最后看哪个盘多文件，就知道哪个了



### 3、配置/data/appdatas/cat/client.xml【包括客户端&服务端】

-	此配置文件的作用是所有的客户端都需要一个地址指向CAT的服务端，比如CAT服务端有三个IP，10.1.1.1，10.1.1.2，10.1.1.3，2280是默认的CAT服务端接受数据的端口，不允许修改，http-port是Tomcat启动的端口，默认是8080，建议使用默认端口。
-	此文件可以通过运维统一进行部署和维护，比如使用puppet等运维工具。
-	不同环境这份文件不一样，比如区分prod环境以及test环境，在美团点评内部一共是2套环境的CAT，一份是生产环境，一份是测试环境

	
```   
<?xml version="1.0" encoding="utf-8"?>
<config mode="client">
	    	<servers>
	                <server ip="10.1.1.1" port="2280" http-port="8080"/>
	                <server ip="10.1.1.2" port="2280" http-port="8080"/>
	                <server ip="10.1.1.3" port="2280" http-port="8080"/>
	    	</servers>
</config>
```

### 4、安装CAT的数据库
- 数据库的脚本文件 script/CatApplication.sql 
- MySQL的一个系统参数：max_allowed_packet，其默认值为1048576(1M)，修改为1000M，修改完需要重启mysql
- 注意：一套独立的CAT集群只需要一个数据库（之前碰到过个别同学在每台cat的服务端节点都安装了一个数据库）

### 5、配置/data/appdatas/cat/datasources.xml【服务端配置】
#### 需要每台CAT集群10.1.1.1，10.1.1.2，10.1.1.3都进行部署

注意：此xml仅仅为模板，请根据自己实际的情况替换jdbc.url,jdbc.user,jdbc.password的实际值。
app数据库和cat数据配置为一样，app库不起作用，为了运行时候代码不报错。

```
<?xml version="1.0" encoding="utf-8"?>

<data-sources>
	<data-source id="cat">
		<maximum-pool-size>3</maximum-pool-size>
		<connection-timeout>1s</connection-timeout>
		<idle-timeout>10m</idle-timeout>
		<statement-cache-size>1000</statement-cache-size>
		<properties>
			<driver>com.mysql.jdbc.Driver</driver>
			<url><![CDATA[${jdbc.url}]]></url>
			<user>${jdbc.user}</user>
			<password>${jdbc.password}</password>
			<connectionProperties><![CDATA[useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&socketTimeout=120000]]></connectionProperties>
		</properties>
	</data-source>
	<data-source id="app">
		<maximum-pool-size>3</maximum-pool-size>
		<connection-timeout>1s</connection-timeout>
		<idle-timeout>10m</idle-timeout>
		<statement-cache-size>1000</statement-cache-size>
		<properties>
			<driver>com.mysql.jdbc.Driver</driver>
			<url><![CDATA[${jdbc.url}]]></url>
			<user>${jdbc.user}</user>
			<password>${jdbc.password}</password>
			<connectionProperties><![CDATA[useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&socketTimeout=120000]]></connectionProperties>
		</properties>
	</data-source>
</data-sources>

```

### 6、启动服务端，通过配置界面，对服务器进行配置

配置链接：http://{ip:port}/cat/s/config?op=serverConfigUpdate

#### 这个只需要更新一次，配置是保存在mysql的数据库里面

CAT节点一共有四个职责

1.	控制台 - 提供给业务人员进行数据查看【默认所有的cat节点都可以作为控制台，不可配置】
2.	消费机 - 实时接收业务数据，实时处理，提供实时分析报表【默认所有的cat节点都可以作为消费机，不可配置】
3.	告警端 - 启动告警线程，进行规则匹配，发送告警（目前仅支持单点部署）【可以配置】
4.	任务机 - 做一些离线的任务，合并天、周、月等报表 【可以配置】

线上做多集群部署，比如说10.1.1.1，10.1.1.2，10.1.1.3这三台机器

1.	建议选取一台10.1.1.1 负责角色有控制台、告警端、任务机，建议配置域名访问CAT，就配置一台机器10.1.1.1一台机器挂在域名下面
2.	10.1.1.2，10.1.1.3 负责消费机处理，这样能做到有效隔离，任务机、告警等问题不影响实时数据处理


配置的sample如下： id="default"是默认的配置信息，server id="10.1.1.1" 如下的配置是表示10.1.1.1这台服务器的节点配置覆盖default的配置信息，比如下面的job-machine，alert-machine，send-machine为true。
[注意这个IP为cat拿到的内网IP，如果你cat部署本地，此IP是看transaction报表下cat的自己上报的IP，用127.0.0.1是没用的。]

```
<?xml version="1.0" encoding="utf-8"?>
<server-config>
   <server id="default">
      <properties>
         <property name="local-mode" value="false"/>
         <property name="job-machine" value="false"/>
         <property name="send-machine" value="false"/>
         <property name="alarm-machine" value="false"/>
         <property name="hdfs-enabled" value="false"/>
         <property name="remote-servers" value="10.1.1.1:8080,10.1.1.2:8080,10.1.1.3:8080"/>
      </properties>
      <storage  local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="7" local-logivew-storage-time="7">
      	<hdfs id="logview" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="logview"/>
      	<hdfs id="dump" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="dump"/>
      	<hdfs id="remote" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="remote"/>
      </storage>
      <consumer>
         <long-config default-url-threshold="1000" default-sql-threshold="100" default-service-threshold="50">
            <domain name="cat" url-threshold="500" sql-threshold="500"/>
            <domain name="OpenPlatformWeb" url-threshold="100" sql-threshold="500"/>
         </long-config>
      </consumer>
   </server>
   <server id="10.1.1.1">
      <properties>
         <property name="job-machine" value="true"/>
         <property name="alert-machine" value="true"/>
	     <property name="send-machine" value="true"/>
      </properties>
   </server>
</server-config>
```

配置说明：

server模型：代表一台机器的配置。如果id为default，代表默认配置；如果id为ip，代表该台服务器的配置

  * property local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;
  * property hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；
  * property job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为 false；
  * property alert-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为 false；
  * property send-machine : 定义当前服务告警是否发送（当时为了解决测试环境开启告警线程，但是最后告警不通知，此配置后续会逐步去除，建议alert-machine开启为true的时候，这个同步为true）
  
storage模型: 定义数据存储配置信息

  * property local-report-storage-time : 定义本地报告存放时长，单位为（天）
  * property local-logivew-storage-time : 定义本地日志存放时长，单位为（天）
  * property local-base-dir : 定义本地数据存储目录
  * property hdfs : 定义HDFS配置信息，便于直接登录系统
  * property server-uri : 定义HDFS服务地址
  * property console : 定义服务控制台信息
  * property remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）
  * ldap : 定义LDAP配置信息（这个可以忽略）
  * ldapUrl : 定义LDAP服务地址（这个可以忽略）
  


### 6、war打包
1. 在cat的源码目录，执行mvn clean install -DskipTests
2. 如果发现cat的war打包不通过，CAT所需要依赖jar都部署在 http://unidal.org/nexus/
3. 可以配置这个公有云的仓库地址到本地的settings路径，理论上不需要配置即可，可以参考cat的pom.xml配置   
4. 如果自行打包仍然问题，请使用下面链接进行下载  http://unidal.org/nexus/service/local/repositories/releases/content/com/dianping/cat/cat-home/3.0.0/cat-home-3.0.0.war 
5. 官方的cat的master版本，重命名为cat.war进行部署，注意此war是用jdk8，服务端请使用jdk8版本
6. 如下是个人本机电脑的测试，下载的jar来自于repo1.maven.org 以及 unidal.org
    

```
Downloading: http://repo1.maven.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.jar
Downloaded: http://repo1.maven.org/maven2/org/apache/commons/commons-email/1.1/commons-email-1.1.jar (30 KB at 9.8 KB/sec)
Downloaded: http://repo1.maven.org/maven2/javax/servlet/jstl/1.2/jstl-1.2.jar (405 KB at 107.7 KB/sec)
Downloaded: http://repo1.maven.org/maven2/com/google/code/javaparser/javaparser/1.0.8/javaparser-1.0.8.jar (235 KB at 55.4 KB/sec)
Downloaded: http://repo1.maven.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.jar (242 KB at 46.9 KB/sec)
Downloaded: http://repo1.maven.org/maven2/org/freemarker/freemarker/2.3.9/freemarker-2.3.9.jar (789 KB at 113.3 KB/sec)
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResServer/1.2.1/WebResServer-1.2.1.jar
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResTagLibrary/1.2.1/WebResTagLibrary-1.2.1.jar
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResTag/1.2.1/WebResTag-1.2.1.jar
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResRuntime/1.2.1/WebResRuntime-1.2.1.jar
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResApi/1.2.1/WebResApi-1.2.1.jar
Downloaded: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResApi/1.2.1/WebResApi-1.2.1.jar (21 KB at 82.7 KB/sec)
Downloading: http://unidal.org/nexus/content/repositories/releases/org/unidal/webres/WebResBase/1.2.1/WebResBase-1.2.1.jar

```


	```
    [INFO] parent ............................................. SUCCESS [ 40.478 s]
	[INFO] cat-client ......................................... SUCCESS [03:47 min]
	[INFO] cat-core ........................................... SUCCESS [ 31.740 s]
	[INFO] cat-hadoop ......................................... SUCCESS [02:50 min]
	[INFO] cat-consumer ....................................... SUCCESS [  3.197 s]
	[INFO] cat-home ........................................... SUCCESS [ 58.964 s]
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
    ``` 
    
### 7、war部署

1.	将cat.war部署到10.1.1.1的tomcat的webapps下，启动tomcat，注意webapps下只允许放一个war，仅仅为cat.war     
2.	如果发现重启报错，里面有NPE等特殊情况，可以检查当前java进程，ps aux | grep java，可能存在之前的tomcat的进程没有关闭，又新启动了一个，导致出问题，建议kill -9 干掉所有的java进程
3.	打开控制台的URL，http://10.1.1.1:8080/cat/s/config?op=routerConfigUpdate  
4.	注意10.1.1.1这个IP需要替换为自己实际的IP链接，修改路由配置只能修改一次即可
5.	修改路由配置为如下，当为如下配置时，10.1.1.1 正常不起消费数据的作用，仅当10.1.1.2以及10.1.1.3都挂掉才会进行实时流量消费


```
<?xml version="1.0" encoding="utf-8"?>
<router-config backup-server="10.1.1.1" backup-server-port="2280">
   <default-server id="10.1.1.2" weight="1.0" port="2280" enable="true"/>
   <default-server id="10.1.1.3" weight="1.0" port="2280" enable="true"/>
</router-config>

```

4.	重启10.1.1.1的机器的tomcat
5.	将cat.war部署到10.1.1.2，10.1.1.3这两台机器中，启动tomcat
6.	cat集群部署完毕，如果有问题，欢迎在微信群咨询，如果文档有误差，欢迎指正以及提交pullrequest


### 8、重启保证数据不丢
1. 请在tomcat重启之前调用当前tomcat的存储数据的链接 http://${ip}:8080/cat/r/home?op=checkpoint，重启之后数据会恢复。【注意重启时间在每小时的整点10-55分钟之间】
2. 线上部署时候，建议把此链接调用存放于tomcat的stop脚本中，这样不需要每次手工调用


========================================================================

### 9、开发环境CAT的部署

1.	请按照如上部署/data/环境目录，数据库配置client.xml, datasources.xml, server.xml这三个配置文件，注意server.xml里面的节点角色，job-machine&alert-machine都可以配置为true
2.	在cat目录中执行 mvn eclipse:eclipse，此步骤会生成一些代码文件，直接导入到工程会发现找不到类
3.	将源码以普通项目到入eclipse中，注意不要以maven项目导入工程
4.	运行com.dianping.cat.TestServer 这个类，即可启动cat服务器
5.	这里和集群版本唯一区别就是服务端部署单节点，client.xml server.xml以及路由地址配置为单台即可


### 10.客户端的集成

1.	参考 http://unidal.org/cat/r/home?op=view&docName=integration
2.	一些埋点的DEMO可以参考cat-home下的testcase，TestSendMessage.java,注意所有埋点cat不支持中文，cat后端存储会过滤掉所有的中文，请使用英文以及简单的符号比如. 来做埋点
3.	一些默认框架埋点的可以参考，cat目录下框架埋点方案集成的文件夹
4.	jar包的集成如下方案
5.	将cat的客户端以及client的依赖包部署到公司私有仓库，检查cat的依赖包可以使用mvn dependency:tree命令
6.	如果公司没有私有仓库，可以请使用cat提供的公有云仓库，http://unidal.org/nexus/
7.	项目的pom可以配置参考cat资源文件的pom.xml文件
  
  ```
     <repositories>
      <repository>
         <id>central</id>
         <name>Maven2 Central Repository</name>
         <layout>default</layout>
         <url>http://repo1.maven.org/maven2</url>
      </repository>
      <repository>
         <id>unidal.releases</id>
         <url>http://unidal.org/nexus/content/repositories/releases/</url>
      </repository>
   </repositories>
   <pluginRepositories>
      <pluginRepository>
         <id>central</id>
         <url>http://repo1.maven.org/maven2</url>
      </pluginRepository>
      <pluginRepository>
         <id>unidal.releases</id>
         <url>http://unidal.org/nexus/content/repositories/releases/</url>
      </pluginRepository>
   </pluginRepositories>
  ```


Copyright and license
===
/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CAT接入公司
===
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/dianping.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ctrip.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/lufax.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ly.png)
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/liepin.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/qipeipu.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/shangping.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/zhenlv.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/oppo.png)


更多接入公司，欢迎在<https://github.com/dianping/cat/issues/753>登记

我们需要知道你对Cat的一些看法以及建议：cat@dianping.com，以及 [Github Issues](https://github.com/dianping/cat/issues)
