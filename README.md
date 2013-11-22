CAT
===
<hr>
Central Application Tracking  [![Build Status](https://travis-ci.org/dianping/cat.png?branch=biz)](https://travis-ci.org/dianping/cat)

Quick Started
---------------------
#####1、安装Java 6+，Maven以及MySQL
#####2、cd到CAT目录下，用maven构建项目
        mvn eclipse:clean eclipse:eclipse
#####3、安装CAT的maven plugin，使用它配置CAT的环境
		cd cat-maven-plugin;mvn cat:install
		
确保系统的临时目录程序拥有读写权限,Linux为/tmp/目录
#####5、如果你安装了hadoop集群，请到/data/appdatas/cat/server.xml中配置对应hadoop信息，并将localmode设置为false。默认情况下，CAT在localmode=true的开发模式下工作。
#####4、运行CAT
开发模式下有两种方式启动：
方式一：
		cd cat-home;mvn jetty:run		
然后浏览http://localhost:2281
方式二：
将项目导入到eclipse中，运行cat-home项目里得‘com.dianping.cat.TestServer’来启动CAT。

Copyright and license
---------------------
Copyright 2013 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
