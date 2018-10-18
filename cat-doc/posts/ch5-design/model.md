## 模型设计

### 监控模型

CAT主要支持以下四种监控模型：

+  **Transaction**	  适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，Transaction用来记录一段代码的执行时间和次数
+  **Event**	   用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小
+  **Heartbeat**	表示程序内定期产生的统计信息, 如CPU利用率, 内存利用率, 连接池状态, 系统负载等
+  **Metric**	  用于记录业务指标、指标可能包含对一个指标记录次数、记录平均值、记录总和，业务指标最低统计粒度为1分钟


### 消息树

CAT监控系统将每次URL、Service的请求内部执行情况都封装为一个完整的消息树、消息树可能包括Transaction、Event、Heartbeat、Metric等信息。

#### 完整的消息树
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll01.png)

#### 可视化消息树
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll02.png)

#### 分布式消息树【一台机器调用另外一台机器】
![Alt text](https://raw.github.com/dianping/cat/master/cat-home/src/main/webapp/images/logviewAll03.png)