CAT [![Build Status](https://travis-ci.org/dianping/cat.png?branch=master)](https://travis-ci.org/dianping/cat)
===
##### CAT基于Java开发的实时应用监控平台，包括实时应用监控，业务监控。[2013-01-06] 

##### CAT支持的监控消息类型包括：
+  **Transaction**	  适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，Transaction用来记录一段代码的执行时间和次数。
+  **Event**	   用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小。
+  **Heartbeat**	表示程序内定期产生的统计信息, 如CPU%, MEM%, 连接池状态, 系统负载等。
+  **Metric**	  用于记录业务指标、指标可能包含对一个指标记录次数、记录平均值、记录总和，业务指标最低统计粒度为1分钟。
+  **Trace**     用于记录基本的trace信息，类似于log4j的info信息，这些信息仅用于查看一些相关信息

消息树
===
CAT监控系统将每次URL、Service的请求内部执行情况都封装为一个完整的消息树、消息树可能包括Transaction、Event、Heartbeat、Metric和Trace信息。

完整的消息树
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll01.png)
可视化消息树
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll02.png)

分布式消息树【一台机器调用另外一台机器】
---------------------

![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll03.png)


Requirements
---------------------
* Java 6或者7 （1.7_79 以下，以上版本jdk会出现不兼容，不支持java1.8）
* Maven 3
* MySQL

Quick Started
---------------------
#####1、在CAT目录下，用maven构建项目
        mvn clean install -DskipTests
        
        如果下载有问题，可以尝试翻墙后下载，可以 git clone git@github.com:dianping/cat.git mvn-repo 下载到本地，这个分支是cat编译需要的依赖的一些jar ，将这些jar放入本地的maven仓库文件夹中。
        
#####2、配置CAT的环境
		mvn cat:install
Note：
* Linux\Mac  需要对/data/appdatas/cat和/data/applogs/cat有读写权限
* Windows    则是对系统运行盘下的/data/appdatas/cat和/data/applogs/cat有读写权限
* 
        此步骤是配置一些cat启动需要的基本数据库配置

#####3、(Optional)如果安装了hadoop集群，需到/data/appdatas/cat/server.xml中配置对应hadoop信息。将localmode设置为false，默认情况下，CAT在开发模式（localmode=true）下工作。

#####4、启动的cat单机版本基本步骤
* 检查下/data/appdatas/cat/ 下面需要的几个配置文件，配置文件在源码script 。
* 在cat目录下执行 mvn install -DskipTests 。
* cat-home打包出来的war包，重新命名为cat.war, 并放入tomcat的webapps 。
* 启动tomcat
* 访问 http://localhost:8080/cat/r
* 具体详细的还可以参考  http://unidal.org/cat/r/home?op=view&docName=deploy  


Copyright and license
---------------------
Copyright 2013 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

CAT接入公司
===
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/dianping.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ctrip.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/lufax.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/ly.png)
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/liepin.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/qipeipu.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/shangping.jpg)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/zhenlv.png)![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logo/oppo.png)


更多接入公司，欢迎在<https://github.com/dianping/cat/issues/753>登记