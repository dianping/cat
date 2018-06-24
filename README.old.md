CAT
 [![Build Status](https://travis-ci.org/dianping/cat.png?branch=master)](https://travis-ci.org/dianping/cat)
 [![GitHub stars](https://img.shields.io/github/stars/dianping/cat.svg?style=social&label=Star&)](https://github.com/dianping/cat/stargazers)
 [![GitHub forks](https://img.shields.io/github/forks/dianping/cat.svg?style=social&label=Fork&)](https://github.com/dianping/cat/fork)

===

[![Join the chat at https://gitter.im/dianping/cat](https://badges.gitter.im/dianping/cat.svg)](https://gitter.im/dianping/cat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

##### CAT基于Java开发的实时应用监控平台，包括实时应用监控，业务监控。[2013-01-06] 

##### CAT支持的监控消息类型包括：
+  **Transaction**	  适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，Transaction用来记录一段代码的执行时间和次数。
+  **Event**	   用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小。
+  **Heartbeat**	表示程序内定期产生的统计信息, 如CPU%, MEM%, 连接池状态, 系统负载等。
+  **Metric**	  用于记录业务指标、指标可能包含对一个指标记录次数、记录平均值、记录总和，业务指标最低统计粒度为1分钟。

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
