__CAT__ 配置文件加载说明
===

## 背景说明

__CAT__监控系统由多个组件组成

![cat 组件关系图](img/cat-cmp.jpg)

* cat-home.war : 服务端组件，负责收集监控信息，分析处理生成报告、作出警告
* cat-agent.war : 监控端组件，负责收集被监控端信息，并上传监控信息到服务端
* cat-client.jar : 客户端组件，负责与服务端进行连接通信，
* cat-core.jar : 核心处理组件，负责具体的与客户端通信服务，解析数据、输出报告
* cat-consumer.jar : 消费处理组件，负责实际的监控数据分析，处理工作
* cat-hadoop.jar : HDFS存储组件
* broker-service.war : 监控服务代理组件


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


### cat-agent 外部配置文件
* /data/appdatas/cat/client.xml，配置监控端连接服务端的信息（内容与cat-home中的配置一样）

* /data/webapps/server.propreties，配置定义监控端 系统信息

### cat-agent 内部配置文件
* cat-agent.war!/WEB-INF/web.xml
 * web应用默认配置文件

* cat-agent.war!/WEB-INF/classes/META-INF/app.properties
 * 首选的应用特征信息配置文件，需要手工添加、定义此配置文件

* cat-agent.war!/WEB-INF/classes/META-INF/cat/client.xml
 * 次选的应用特征信息配置文件

* cat-agent.war!/WEB-INF/classes/META-INF/plexus/components.xml
 * 让plexus加载类定义的配置文件，


## 配置文件加载流程

### cat-home.war配置文件加载

* Web容器加载web.xml；

   ```cat-home.war!/WEB-INF/web.xml```

* plexus类容器加载components.xml；

   ```cat-home.war!/WEB-INF/classes/META-INF/plexus/components.xml```

* 在CatHomeModule类中，ServerConfigManager加载server.xml

    ```/data/appdatas/cat/server.xml```

### cat-agent.war配置文件加载

* Web容器加载web.xml配置文件；

   ```cat-agent.war!/WEB-INF/web.xml```

* plexus类容器加载components.xml配置文件；

   ```cat-agent.war!/WEB-INF/classes/META-INF/plexus/components.xml```

* 在CatClientModule类中，ClientConfigManager加载监控端配置
 
 1. 先尝试 加载外部client.xml，作为全局配置（当前机器共用的特征信息）

    ```/data/appdatas/cat/client.xml```

 2. 再尝试 加载app.properties，获取应用特征信息

   ```cat-agent.war!/WEB-INF/classes/META-INF/app.properties```

 3. 加载不成功，尝试加载内部client.xml

   ```cat-agent.war!/WEB-INF/classes/META-INF/cat/client.xml```

 4. 最后把应用特征信息，合并到全局配置信息，作为当前应用特征信息。

### 运行期内同步服务端的配置信息

在ChannelManager类中，

* 每10秒钟会检测一次服务端对外服务配置信息是否变化（获取服务端的路由信息）；
* 若配置信息有变化，重新创建与服务端的通信通道