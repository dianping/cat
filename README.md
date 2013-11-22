CAT [![Build Status](https://travis-ci.org/dianping/cat.png?branch=biz)](https://travis-ci.org/dianping/cat)
===
CAT的全称是Central Application Tracking，是基于Java开发的实时应用监控平台，包括实时系统监控、应用监控以及业务监控。
CAT主要通过以下几种埋点类型收集信息：
* Event	用来记录次数，表名单位时间内消息发生次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小。
* Transaction	适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控。
* Heartbeat	表示程序内定期产生的统计信息, 如CHPU%, MEM%, 连接池状态, 系统负载等。
* Metric	用于业务监控埋点的API。


Requirements
---------------------
* Java 6
* Maven
* MySQL

Quick Started
---------------------
#####1、在CAT目录下，用maven构建项目
        mvn clean install
#####2、配置CAT的环境
		mvn cat:install
#####3、(Optional)如果安装了hadoop集群，需到/data/appdatas/cat/server.xml中配置对应hadoop信息。将localmode设置为false，默认情况下，CAT在开发模式（localmode=true）下工作。
#####4、运行CAT
		cd cat-home;mvn jetty:run
然后打开浏览器，输入http://localhost:2281/cat。

或者在cat目录下输入
				
		mvn eclipse:clean eclipse:eclipse
然后将项目导入到eclipse中，运行cat-home项目里得‘com.dianping.cat.TestServer’来启动CAT。

Copyright and license
---------------------
Copyright 2013 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
