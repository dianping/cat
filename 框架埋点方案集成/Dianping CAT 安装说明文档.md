# Dianping CAT 安装说明文档

## 一、系统要求

### 1. 操作系统及硬件环境

监听端：

* 根据业务系统需求确定

服务端：

* 内存 4G 以上
* 硬盘 100G 以上
* 操作系统 Windows或Linux操作系统（建议选用Linux操作系统）

### 2. 运行环境

* Java 6 以上
* Web 应用服务器，如：Apache Tomcat、JBoss Application Server、WebSphere Application Server、WebLogic Application Server（可选项，内置Netty应用服务器）
* MySQL 数据库
* Maven 3 以上（只编译和安装时需要）

注意：安装时需要拥有计算机管理员权限。

### 3. 网络环境

要求连接到互联网或通过代理上网。

## 二、安装包文件清单

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

 * jdk-7u79-linux-i586.tar.gz (linux 32位系统)
 * jdk-7u79-linux-x64.tar.gz (linux 64位系统)
 * jdk-7u79-windows-i586.exe (windows 32位系统)
 * jdk-7u79-windows-x64.exe (windows 32位系统)

* [Apache Tomcat](http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.57/bin/)

 * apache-tomcat-7.0.57.tar.gz (linux 系统)
 * apache-tomcat-7.0.57-windows-x86.zip (windows 32位系统)
 * apache-tomcat-7.0.57-windows-x64.zip (windows 32位系统)

* [MySQL](http://www.mysql.com/downloads/)

 * MySQL Server 5.1

* [Maven](http://maven.apache.org/download.cgi)

 * apache-maven-3.1.1-bin.tar.gz (linux 系统)
 * apache-maven-3.1.1-bin.zip (windows 系统)

* [dianping CAT]

 * [Cat source code](https://github.com/dianping/cat/archive/master.zip) (直接下载)
  `https://github.com/dianping/cat/archive/master.zip`
 * 通过 Git Clone
  `https://github.com/dianping/cat.git`

## 三、安装操作

### 1、 编译源码，构建war包

* 前提条件

 1. 已安装、配置JDK;
 2. 已安装、配置MAVEN;
 3. 已下载CAT源码;

* 操作步骤

 1. 进入监控系统源码的cat目录

    cd /source/cat

 2. 显示确认目录结构

    dir

    ![目录结构](http://upload-images.jianshu.io/upload_images/424045-e9fad099e5c4c8f5.jpg)

 3. 运行 MAVEN 打包安装命令

    mvn clean install -DskipTests

 4. 执行完成后，

   * 编译构造好的 war 安装到 Maven 仓库中。

### 2、自动创建库表、配置文件

* 前提条件

 1. CAT 安装包已构建
 2. /data/appdatas/cat/ 目录有读写权限

* 操作步骤

 1. 运行 CAT 安装插件命令

    maven cat:install

 2. 按提示输入数据库配置参数

    ![数据库配置](http://upload-images.jianshu.io/upload_images/424045-7e5de138226fe347.jpg)
    
	（_红线标识输入的配置信息_）

 3. 执行完成后，

 * 数据库中创建cat表空间，并创建所有表结构;

 * 在/data/appdatas/cat/目录中，生成三个配置文件：client.xml、server.xml、datasources.xml（windows系统中，/data目录与源码目录在一个系统盘）

 4. 补充说明
	
	此部分操作，可手工完成，安装MySQL好数据库后,
	
	* 登录MySQL,创建cat表空间
	  
	  create database cat

	* 执行监控系统源码/source/cat/script/Cat.sql脚本完成表结构的创建
	 
	 source /source/cat/script/Cat.sql
	
	* 拷贝监控系统源码/source/cat/script/目录下的client.xml、server.xml、datasources.xml到/data/appdatas/cat/目录中

	 cp /source/cat/script/*.xml /data/appdatas/cat/

### 3、修改监控系统CAT服务配置

安装创建的配置信息都是默认值，需要按实际情况修改，整个系统才可正常运行。

几项假设 

 * cat.war 包部署在10.8.40.26、10.8.40.27、10.8.40.28三台机器上,10.8.40.26为三台机器中的主服务器，TCP端口只能局域网内访问；
 * web应用服务器采用 Tomcat 7；
 * 数据库采用 MySQL 5.1 ,安装在10.8.40.147上；
 * 暂不启用HDFS存储服务；
 * 暂不启用LDAP服务；

* 前提条件

 1. CAT 安装包已构建
 2. 数据库和表结构已创建
 3. /data/appdatas/cat/ 目录下的配置文件已生成

* 操作步骤
 
 1. 修改客户端配置文件

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

 2. 修改数据库配置

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

 3. 修改服务端服务配置

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
		<ldap ldapUrl="ldap://10.8.40.21:389/DC=dianpingoa,DC=com"/>
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
  * ldap : 定义LDAP配置信息（这个可以忽略）
  * ldapUrl : 定义LDAP服务地址（这个可以忽略）


 4. 发布启动 cat-home 服务

  1. 拷贝监控系统源码/source/cat/cat-home/target/目录下的cat-alpha-1.3.3.war到web应用服务器的发布目录（如：$TOMCAT_HOME$/webapps/）,并修改war包名称为cat.war

    `cp /source/cat/cat-home/cat-alpha-1.3.3.war /usr/local/tomcat7/webapps/cat.war`

  2. 启动应用服务器

    cd /usr/local/tomcat7/bin/
    ./startup.sh

 5. 登入 cat-home 系统，修改路由配置

    打开浏览器，输入[http://10.8.40.26:8080/cat/](http://10.8.40.26:8080/cat/)
    
	![cat-route.jpg](http://upload-images.jianshu.io/upload_images/424045-e07ca5912a562d16.jpg)
    
	选择 配置-->全局警告配置-->客户端路由,或者在浏览器地址栏中直接输入http:/10.8.40.26:8080/cat/s/config?op=routerConfigUpdate，打开客户端路由配置界面。

     * 把backup-server设置为当前服务器对外IP地址，端口固定为2280;
     * default-server定义可跳转的路由地址，可以设置多个。default-server的id属性配置可路由的cat-home服务IP地址，端口固定为2280;若需要禁用路由地址，可把enable设置为false。
	 * 点击“提交”按钮，保存修改的路由配置

 6. 复制配置到27、28两机器

　* 拷贝 10.8.40.26机器/data/appdatas/cat/目录中client.xml、server.xml、datasources.xml三个配置文件到27、28两机器相同目录中
  * 修改server.xml配置中的 job-machine 和 alert-machine属性，都设置为false,禁用生成报告和报警功能，只开启监听功能
  * 启动27、28上的Tomcat,开启cat服务，完成服务端的配置及启动

  （*若服务端只分配一台服务器，按10.8.40.26完成安装配置即可*）



## 附注

如果在linux操作系统上安装cat-home，并且有联网权限，可以选择一键安装CAT：

* 获取监控系统源码/source/cat/script/目录全部脚本，
* 拷贝到linux操作系统机器上任意目录
* 执行/source/cat/script/install-shell/installAll.sh脚本，

执行结果：

* 下载、安装、配置 jdk1.6.0_45
* 下载、安装、配置 apache-maven-3.2.5
* 下载、安装、配置 MySQL
* 下载、安装 apache-tomcat-7.0.57
* 在线安装 Git
* 通过Git下载CAT,并编译、安装、配置CAT

启动CAT后，按"修改监控系统CAT服务配置"说明进行后继配置


