**CAT** [![Build Status](https://travis-ci.org/dianping/cat.png?branch=master)](https://travis-ci.org/dianping/cat)


什么是CAT
===

#### Cat是基于Java开发的实时应用监控平台，为美团点评提供了全面的实时监控告警服务。

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


安装说明
===

#### 1. 服务端操作系统及硬件环境

* 内存 4G 以上
* 硬盘 100G 以上
* 操作系统 Windows或Linux操作系统（建议选用Linux操作系统）

#### 2. 运行环境

* Java 7 以上
* Web 应用服务器，如：Apache Tomcat、JBoss Application Server、WebSphere Application Server、WebLogic Application Server（可选项，内置Netty应用服务器）
* MySQL 数据库
* Maven 3 以上（只编译和安装时需要）

注意：安装时需要拥有计算机管理员权限。

#### 快速开始

##### 1. 编译源码，构建war包

* 前提条件

 1. 已安装、配置JDK;
 2. 已安装、配置MAVEN;
 3. 已下载CAT源码;

* 操作步骤

 1. 进入监控系统源码的cat目录

 2. 运行 MAVEN 打包安装命令
 
 	mvn clean install -DskipTests

 3. 执行完成后，编译构造好的 war 安装到 Maven 仓库中。

##### 2. 创建库表

* 操作步骤

 1. 登录MySQL,创建cat表空间

    create database cat

 2. 执行监控系统源码/source/cat/script/Cat.sql脚本完成表结构的创建

    source /source/cat/script/Cat.sql

##### 3. 拷贝配置文件

* 前提条件

 1. /data/appdatas/cat/ 目录有读写权限

* 操作步骤

 1. 拷贝监控系统源码/source/cat/script/目录下的client.xml、server.xml、datasources.xml到/data/appdatas/cat/目录中
 
    cp /source/cat/script/*.xml /data/appdatas/cat/
    
##### 4. 修改配置文件

安装创建的配置信息都是默认值，需要按实际情况修改，整个系统才可正常运行。

几项假设 

 * cat.war 包部署在10.8.40.26、10.8.40.27、10.8.40.28三台机器上,10.8.40.26为三台机器中的主服务器，TCP端口只能局域网内访问；
 * 数据库采用 MySQL ,安装在10.8.40.147上；
 * 暂不启用HDFS存储服务；

###### 1. 修改客户端配置文件

　　打开/data/appdatas/cat/client.xml客户端配置文件，

	<config mode="client"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema"
	xsi:noNamespaceSchemaLocation="config.xsd">
		<servers>
			<server ip="10.8.40.26" port="2280" http-port="8080" />
			<server ip="10.8.40.27" port="2280" http-port="8080" />
			<server ip="10.8.40.28" port="2280" http-port="8080" />
		</servers>
	</config>

 配置说明：

  * mode : 定义配置模式，固定值为client;--暂未使用
  * servers : 定义多个服务端信息;
  * server : 定义某个服务端信息;
  * ip : 配置服务端（cat-home）对外IP地址
  * port : 配置服务端（cat-home）对外TCP协议开启端口，固定值为2280;
  * http-port : 配置服务端（cat-home）对外HTTP协议开启端口, 如：tomcat默认是8080端口，若未指定，默认为8080端口;

###### 2. 修改数据库配置

　　打开/data/appdatas/cat/datasources.xml数据库配置文件，

	<data-sources>
		<data-source id="cat">
			<maximum-pool-size>3</maximum-pool-size>
			<connection-timeout>1s</connection-timeout>
			<idle-timeout>10m</idle-timeout>
			<statement-cache-size>1000</statement-cache-size>
			<properties>
				<driver>com.mysql.jdbc.Driver</driver>
				<url><![CDATA[jdbc:mysql://10.8.40.147:3306/cat]]></url>
				<user>root</user>
				<password>mysql</password>
				<connectionProperties>
					<![CDATA[useUnicode=true&autoReconnect=true]]>
				</connectionProperties>
			</properties>
		</data-source> 
		<data-source id="app">
			<maximum-pool-size>3</maximum-pool-size>
			<connection-timeout>1s</connection-timeout>
			<idle-timeout>10m</idle-timeout>
			<statement-cache-size>1000</statement-cache-size>
			<properties>
				<driver>com.mysql.jdbc.Driver</driver>
				<url><![CDATA[jdbc:mysql://10.8.40.147:3306/cat]]></url>
				<user>root</user>
				<password>mysql</password>
				<connectionProperties>
					<![CDATA[useUnicode=true&autoReconnect=true]]>
				</connectionProperties>
			</properties>
		</data-source>
	</data-sources>

 配置说明：

  * 生成配置文件时，输入的数据库连接信息已写入此文件，如不换数据库，不用做任何修改
  * 主要修改项为：url（数据库连接地址）、user（数据库用户名）、password（数据用户登录密码）

###### 3. 修改服务端服务配置

　　打开/data/appdatas/cat/server.xml服务端服务配置文件，

	<config local-mode="false" hdfs-machine="false" job-machine="true" alert-machine="true">
		<storage local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="7" local-logivew-storage-time="7">
			<hdfs id="logview" max-size="128M" server-uri="hdfs://10.8.40.31/user/cat" base-dir="logview"/>
			<hdfs id="dump" max-size="128M" server-uri="hdfs://10.8.40.32/user/cat" base-dir="dump"/>
			<hdfs id="remote" max-size="128M" server-uri="hdfs://10.8.40.33/user/cat" base-dir="remote"/>
		</storage>
		<console default-domain="Cat" show-cat-domain="true">
			<remote-servers>10.8.40.26:8080,10.8.40.27:8080,10.8.40.28:8080</remote-servers>
		</console>
	</config>

 配置说明：

  * local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;
  * hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；
  * job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为 false；
  * alert-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为 false；
  * storage : 定义数据存储配置信息
  * local-report-storage-time : 定义本地报告存放时长，单位为（天）
  * local-logivew-storage-time : 定义本地日志存放时长，单位为（天）
  * local-base-dir : 定义本地数据存储目录
  * hdfs : 定义HDFS配置信息，便于直接登录系统
  * server-uri : 定义HDFS服务地址
  * console : 定义服务控制台信息
  * remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）

##### 5. 启动 cat-home 服务

  1. 拷贝监控系统源码/source/cat/cat-home/target/目录下的cat-x.x.x.war到web应用服务器的发布目录（如：$TOMCAT_HOME$/webapps/）,并修改war包名称为cat.war

  2. 启动应用服务器
  
##### 6. 登入 cat-home 系统，修改路由配置

  1. 打开浏览器，输入localhost:8080/cat/r

  2. 选择 配置-->全局系统配置-->客户端路由,打开客户端路由配置界面。

     * 把backup-server设置为当前服务器对外IP地址，端口固定为2280;
     * default-server定义可跳转的路由地址，可以设置多个。default-server的id属性配置可路由的cat-home服务IP地址，端口固定为2280;若需要禁用路由地址，可把enable设置为false。
	 * 点击“提交”按钮，保存修改的路由配置


Copyright and license
===
Copyright 2013 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


CAT接入公司
===
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/dianping.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ctrip.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/lufax.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ly.png)
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/liepin.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/qipeipu.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/shangping.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/zhenlv.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/oppo.png)

更多接入公司，欢迎在<https://github.com/dianping/cat/issues/753>登记