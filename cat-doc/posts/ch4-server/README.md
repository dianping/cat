## 服务端部署


### cat组件

cat主要由以下组件组成：

* **cat-home**: 服务端组件，负责收集监控信息，分析处理生成报告、执行告警
* **cat-client**: 客户端组件，负责与服务端进行连接通信，
* **cat-core**: 核心处理组件，负责具体的与客户端通信服务，解析数据、输出报告
* **cat-consumer** : 消费处理组件，负责实际的监控数据分析，处理工作
* **cat-hadoop** : HDFS存储组件



### 配置文件

cat主要有三个外部配置文件,分别是：

* /data/appdatas/cat/client.xml
   
 * 配置连接（其它）服务端的信息,如IP地址、tcp端口、http端口


* /data/appdatas/cat/server.xml

 * 定义服务端启用服务；
 * 数据存储方式、策略及存储配置信息；
 * 服务端服务群信息（如服务器的IP地址、tcp端口、http端口、服务的权重、服务配置状态）


* /data/appdatas/cat/datasources.xml

 * 定义数据库连接信息

如何设置配置文件，请见下文介绍。



### 安装说明

#### 1. 系统要求

##### 1. 操作系统及硬件环境

客户端：

* 根据业务系统需求确定

服务端：

* 内存 4G 以上
* 硬盘 100G 以上
* 操作系统 Windows或Linux操作系统（建议选用Linux操作系统）

##### 2. 运行环境

* Java 7 以上
* Web 应用服务器，如：Apache Tomcat、JBoss Application Server、WebSphere Application Server、WebLogic Application Server（可选项，内置Netty应用服务器）
* MySQL 数据库
* Maven 3 以上（只编译和安装时需要）

注意：安装时需要拥有计算机管理员权限。

##### 3. 网络环境

要求连接到互联网或通过代理上网。

#### 2. 安装包文件清单

* [Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

* [Apache Tomcat](http://tomcat.apache.org/)

* [MySQL](http://www.mysql.com/downloads/)

* [Maven](http://maven.apache.org/download.cgi)

* [CAT](https://github.com/dianping/cat)

#### 3. 安装操作

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


#### 恭喜您，您已成功部署Cat！