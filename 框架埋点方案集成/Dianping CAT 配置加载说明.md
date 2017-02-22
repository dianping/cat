__CAT__ 配置文件加载说明
===

## 背景说明

__CAT__监控系统由多个组件组成

![cat 组件关系图](img/cat-cmp.jpg)

* cat-home.war : 服务端组件，负责收集监控信息，分析处理生成报告、作出警告
* cat-client.jar : 客户端组件，负责与服务端进行连接通信，
* cat-core.jar : 核心处理组件，负责具体的与客户端通信服务，解析数据、输出报告
* cat-consumer.jar : 消费处理组件，负责实际的监控数据分析，处理工作
* cat-hadoop.jar : HDFS存储组件


## 配置文件清单说明

### cat-home.war 外部配置文件

cat-home.war 有三个外部配置文件,分别是：

* /data/appdatas/cat/client.xml，
   
 * 配置连接（其它）服务端的信息,如IP地址、tcp端口、http端口

* /data/appdatas/cat/server.xml，

 * 定义服务端启用服务；
 * 数据存储方式、策略及存储配置信息；
 * 服务端服务群信息（如服务器的IP地址、tcp端口、http端口、服务的权重、服务配置状态）

* /data/appdatas/cat/datasources.xml，

 * 配置服务端连接数据库的信息
	
### cat-home.war 内置配置文件
* cat-home.war!/WEB-INF/web.xml
 * web应用默认配置文件

* cat-home.war!/WEB-INF/classes/META-INF/app.properties
 * 首选的应用特征信息配置文件，默认内容为：app.name=cat

* cat-home.war!/WEB-INF/classes/META-INF/cat/client.xml
 * 次选的应用特征信息配置文件

* cat-home.war!/WEB-INF/classes/META-INF/plexus/components.xml
 * plexus类容器加载类定义的配置文件，


## 配置文件加载流程

### cat-home.war配置文件加载

* Web容器加载web.xml；

   ```cat-home.war!/WEB-INF/web.xml```

* plexus类容器加载components.xml；

   ```cat-home.war!/WEB-INF/classes/META-INF/plexus/components.xml```

* 在CatHomeModule类中，ServerConfigManager加载server.xml

    ```/data/appdatas/cat/server.xml```


### 运行期内同步服务端的配置信息

在ChannelManager类中，

* 每10秒钟会检测一次服务端对外服务配置信息是否变化（获取服务端的路由信息）；
* 若配置信息有变化，重新创建与服务端的通信通道