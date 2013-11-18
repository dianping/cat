CAT
===
<hr>
Central Application Tracking  [![Build Status](https://travis-ci.org/dianping/cat.png?branch=biz)](https://travis-ci.org/dianping/cat)

Quick Started
---------------------
#####1、安装Java 6+，Maven以及MySQL
#####2、cd到CAT目录下，用maven构建项目
        mvn eclipse:eclipse
#####3、安装CAT的maven plugin，使用它配置CAT的环境
		cd cat-maven-plugin;mvn install;mvn cat:install
		
确保系统的临时目录程序拥有读写权限,Linux为/tmp/目录
#####4、运行CAT
		cd cat-home;mvn jetty:run
		
然后浏览http://localhost:2281
#####5、如果你安装了hadoop集群，请配置/data/appdatas/cat/server.xml中对应hadoop信息，如果没有hadoop集群，server.xml中localmode必须为true,CAT只能在开发环境工作。【可选】

#####6、导入项目到eclipse中，在开发时，可以运行testcase启动项目 ‘com.dianping.cat.TestServer’来启动CAT

Copyright and license
---------------------
Copyright 2013 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
