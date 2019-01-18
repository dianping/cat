## 服务端部署

### 0. 服务端口清单

| 端口 | 服务 | 其他说明 |
| ------ | ------ | ------ |
| 8080 | web控制台 | web页面的访问连接为：http://127.0.0.1:8080/cat/r |
| 2280 | 服务端 | 数据上报的server端端口 |
| 33306 | 数据库 | 容器内端口3306，宿主机33306 |

### 1. Docker快速部署

#### 说明

1. 支持本机模式快速部署。
2. docker/Dockerfile包含CAT依赖的环境，可以作为源码部署的参考。
3. 默认的运行方式是集成了一个mysql镜像，可以修改为自己的mysql的详细配置。默认运行的mysql服务，将mysql数据挂载到了`docker/mysql/volume`中。

#### 容器构建

```bash
cd docker
docker-compose up
```

第一次运行以后，数据库中没有表结构，需要通过下面的命令创建表：
    
```bash
docker exec <container_id> bash -c "mysql -uroot -Dcat < /init.sql"
```
    
说明：<container_id>需要替换为容器的真实id。通过 docker ps 可以查看到mysql容器id

#### 依赖配置说明

1. datasources.xml
    - CAT数据库配置，默认配置是mysql镜像，可以按需替换
2. docker-compose.yml
    - 通过docker-compose启动的编排文件，文件中包含cat和mysql。可以屏蔽掉mysql的部分，并且修改cat的环境变量，改为真实的mysql连接信息。
3. client.xml
    - 不是必须的，配置client以后，cat会将运行的本机也作为一个监控端。
4. datasources.sh
    - 辅助脚本，脚本作用时修改`datasources.xml`，使用环境变量中制定的mysql连接信息。（通过sed命令替换）


### 2. 源码部署

#### CAT安装环境

* Linux 2.6以及之上（2.6内核才可以支持epoll），线上服务端部署请使用Linux环境，Mac以及Windows环境可以作为开发环境，美团点评内部CentOS 6.5
* Java  6，7，8，服务端推荐是用jdk7的版本，客户端jdk6、7、8都支持
* Maven 3及以上
* MySQL 5.6，5.7，其他版本MySQL都不建议使用，不清楚兼容性（Mysql8肯定不兼容）
* J2EE容器建议使用tomcat，建议使用推荐版本7.*.*或8.0.*
* Hadoop环境可选，一般建议规模较小的公司直接使用磁盘模式，可以申请CAT服务端，500GB磁盘或者更大磁盘，这个磁盘挂载在/data/目录上


#### CAT组件

CAT主要由以下组件组成：

* **cat-home**: 服务端组件，负责收集监控信息，分析处理生成报告、执行告警
* **cat-client**: 客户端组件，负责与服务端进行连接通信，
* **cat-core**: 核心处理组件，负责具体的与客户端通信服务，解析数据、输出报告
* **cat-consumer** : 消费处理组件，负责实际的监控数据分析，处理工作
* **cat-hadoop** : HDFS存储组件


#### 安装CAT集群步骤概览

1. 初始化Mysql数据库，`一套CAT集群需要部署一个数据库`，数据库脚本script/CatApplication.sql
2. （本机模式忽略）准备三台CAT服务器，IP假设为10.1.1.1，10.1.1.2，10.1.1.3，下面的集群模式安装会以这些IP为例
3. 初始化/data/目录，配置几个配置文件/data/appdatas/cat/*.xml 几个配置文件，具体下面有详细说明
4. `打包并重命名为cat.war`，放入tomcat容器webapps根目录下，并启动tomcat
5. 修改服务器配置、及路由配置，重启tomcat
6. 默认的cat的登陆用户名密码为admin,admin


#### **步骤1：** 部署tomcat

- 调整启动参数，修改 catalina.sh文件【服务端】

    1. 需要每台CAT集群10.1.1.1，10.1.1.2，10.1.1.3都进行部署
    2. 建议使用cms gc策略
    3. 建议cat的使用堆大小至少10G以上，开发环境启动2G堆启动即可

    ```bash
    CATALINA_OPTS="$CATALINA_OPTS -server -Djava.awt.headless=true -Xms25G -Xmx25G -XX:PermSize=256m -XX:MaxPermSize=256m -XX:NewSize=10144m -XX:MaxNewSize=10144m -XX:SurvivorRatio=10 -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:MaxTenuringThreshold=13 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+UseCMSCompactAtFullCollection -XX:+CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=9 -XX:CMSInitiatingOccupancyFraction=60 -XX:+CMSClassUnloadingEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:-ReduceInitialCardMarks -XX:+CMSPermGenSweepingEnabled -XX:CMSInitiatingPermOccupancyFraction=70 -XX:+ExplicitGCInvokesConcurrent -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="%CATALINA_HOME%\conf\logging.properties" -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC -Xloggc:/data/applogs/heap_trace.txt -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/applogs/HeapDumpOnOutOfMemoryError -Djava.util.Arrays.useLegacyMergeSort=true"
    ```

- 修改中文乱码 tomcat conf 目录下 server.xml

    ```xml
    <Connector port="8080" protocol="HTTP/1.1"
               URIEncoding="utf-8"    connectionTimeout="20000"
                   redirectPort="8443" />  <!-- 增加  URIEncoding="utf-8"  -->  
    ```


#### **步骤2：** 程序对于/data/目录具体读写权限（重要）

- Linux

    1. 要求/data/目录能进行读写操作，如果/data/目录不能写，建议使用linux的软链接链接到一个固定可写的目录。
    2. 此目录会存一些CAT必要的配置文件以及运行时候的数据存储目录。
    
        ```bash
        mkdir /data
        chmod 777 /data/ -R
        ```
- Windows

    对程序运行盘下的/data/appdatas/cat和/data/applogs/cat有读写权限。`例如cat服务运行在e盘的tomcat中，则需要对e:/data/appdatas/cat和e:/data/applogs/cat有读写权限。建议windows用户在所有盘都建一个，然后最后check下哪个盘里面有文件增加，就知道使用的是哪个盘了，然后在把其他盘的文件删掉。注意cat会在/data/appdatas/cat/下面增加一个*.mark文件`
    

#### **步骤3：** 配置/data/appdatas/cat/client.xml

- 说明

    1. 此配置文件的作用是所有的客户端都需要一个地址指向CAT的服务端
    2. 此文件可以通过运维统一进行部署和维护，比如使用puppet等运维工具
    3. 不同环境这份文件不一样，比如区分prod环境以及test环境，在美团点评内部一共是2套环境的CAT，一份是生产环境，一份是测试环境

- 本机模式

    ```xml   
    <?xml version="1.0" encoding="utf-8"?>
    <config mode="client">
        <servers>
            <server ip="127.0.0.1" port="2280" http-port="8080"/>
        </servers>
    </config>
    ```

- 集群模式

    假设CAT服务端有三个IP，10.1.1.1，10.1.1.2，10.1.1.3，`2280是默认的CAT服务端接受数据的端口，不允许修改，http-port是Tomcat启动的端口，默认是8080，建议使用默认端口`
        
    ```xml  
    <?xml version="1.0" encoding="utf-8"?>
    <config mode="client">
        <servers>
            <server ip="10.1.1.1" port="2280" http-port="8080"/>
            <server ip="10.1.1.2" port="2280" http-port="8080"/>
            <server ip="10.1.1.3" port="2280" http-port="8080"/>
        </servers>
    </config>
    ```

#### **步骤4：** 安装CAT的数据库

- 数据库的脚本文件 script/CatApplication.sql 

    ```bash
    mysql -uroot -Dcat < CatApplication.sql
    ```

- MySQL的一个系统参数：`max_allowed_packet`，其默认值为1048576(1M)，修改为1000M，修改完需要重启mysql

- 说明：

    1. 注意1：`一套独立的CAT集群只需要一个数据库`（之前碰到过个别同学在每台cat的服务端节点都安装了一个数据库）
    2. 注意2：`数据库编码使用utf8mb4，否则可能造成中文乱码等问题`


#### **步骤5：** 配置/data/appdatas/cat/datasources.xml

```xml
<?xml version="1.0" encoding="utf-8"?>

<data-sources>
	<data-source id="cat">
		<maximum-pool-size>3</maximum-pool-size>
		<connection-timeout>1s</connection-timeout>
		<idle-timeout>10m</idle-timeout>
		<statement-cache-size>1000</statement-cache-size>
		<properties>
			<driver>com.mysql.jdbc.Driver</driver>
			<url><![CDATA[jdbc:mysql://127.0.0.1:3306/cat]]></url>  <!-- 请替换为真实数据库URL及Port  -->
			<user>root</user>  <!-- 请替换为真实数据库用户名  -->
			<password>root</password>  <!-- 请替换为真实数据库密码  -->
			<connectionProperties><![CDATA[useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&socketTimeout=120000]]></connectionProperties>
		</properties>
	</data-source>
</data-sources>
```

- 说明

    1. 此xml仅仅为模板，请根据自己实际的情况`替换jdbc.url,jdbc.user,jdbc.password的实际值 `。
    2. `集群模式每台CAT集群10.1.1.1，10.1.1.2，10.1.1.3都需要部署此文件`

#### **步骤6：** war打包

- 源码构建

    1. 在cat的源码目录，执行`mvn clean install -DskipTests`  [建议使用master代码分支，所有的bug fix都会同步在master分支上]
    2. 如果发现cat的war打包不通过，CAT所需要依赖jar都部署在 http://unidal.org/nexus/
    3. 可以配置这个公有云的仓库地址到本地Maven配置（一般为~/.m2/settings.xml)，理论上不需要配置即可，可以参考cat的pom.xml配置：   
    
    ```xml
    <repositories>
      <repository>
         <id>central</id>
         <name>Maven2 Central Repository</name>
         <layout>default</layout>
         <url>http://repo1.maven.org/maven2</url>
      </repository>
      <repository>
         <id>unidal.releases</id>
         <url>http://unidal.org/nexus/content/repositories/releases/</url>
      </repository>
    </repositories>
    ```

- 官方下载

    1. 如果自行打包仍然问题，请使用下面链接进行下载：  
    
        http://unidal.org/nexus/service/local/repositories/releases/content/com/dianping/cat/cat-home/3.0.0/cat-home-3.0.0.war 
    
    2. 官方的cat的master版本，`重命名为cat.war进行部署，注意此war是用jdk8，服务端请使用jdk8版本`
    
#### **步骤7：** war部署

- 本机模式

    1.	将cat.war部署到本机tomcat的webapps下，注意这个目录下仅仅保留一个cat.war，其他相关东西一概删掉，启动tomcat。  注意这边一定要检查下tomcat重启之后是不是只有一个tomcat进程，很多人tomcat重启其实之前tomcat进程没有杀干净，导致后续启动出错       
    2.	打开控制台的URL，http://127.0.0.1:8080/cat/s/config?op=routerConfigUpdate  
    3.  把下面的xml文件的127.0.0.1 替换为你本机的实际的内网IP，比如说192.168.1.1 
   
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <router-config backup-server="127.0.0.1" backup-server-port="2280">
       <default-server id="127.0.0.1" weight="1.0" port="2280" enable="true"/>
       <network-policy id="default" title="default" block="false" server-group="default_group">
       </network-policy>
       <server-group id="default_group" title="default-group">
          <group-server id="127.0.0.1"/>
       </server-group>
    </router-config>
    ```

- 集群模式

    1.	将cat.war部署到10.1.1.1的tomcat的webapps下，注意这个目录下仅仅保留一个cat.war，其他相关东西一概删掉，启动tomcat，注意这边一定要检查下tomcat重启之后是不是只有一个tomcat进程，很多人tomcat重启其实之前tomcat进程没有杀干净，导致后续启动出错     
    2.	打开控制台的URL，http://10.1.1.1:8080/cat/s/config?op=routerConfigUpdate  
    3.  这里面你需要根据实际你自己集群IP来替换下面xml中的10.1.1.1，10.1.1.2，10.1.1.3
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <router-config backup-server="10.1.1.1" backup-server-port="2280">
       <default-server id="10.1.1.1" weight="1.0" port="2280" enable="false"/>
       <default-server id="10.1.1.2" weight="1.0" port="2280" enable="true"/>
       <default-server id="10.1.1.3" weight="1.0" port="2280" enable="true"/>
       <network-policy id="default" title="default" block="false" server-group="default_group">
       </network-policy>
       <server-group id="default_group" title="default-group">
          <group-server id="10.1.1.2"/>
          <group-server id="10.1.1.3"/>
       </server-group>
       <domain id="cat">
          <group id="default">
             <server id="10.1.1.2" port="2280" weight="1.0"/>
             <server id="10.1.1.3" port="2280" weight="1.0"/>
          </group>
       </domain>
    </router-config>
    ```
    
    3.	重启10.1.1.1的机器的tomcat
    4.	将cat.war部署到10.1.1.2，10.1.1.3这两台机器中，启动tomcat

    说明：

    1. 注意10.1.1.1，10.1.1.2，10.1.1.3这几个IP需要`替换为自己实际的IP`，修改路由配置仅仅需要修改一次即可，这部分数据会存入mysql中。
    2. `enable=false表示机器不可用，10.1.1.1机器不做为消费机集群。所有客户端上报的消息都由于10.1.1.2，10.1.1.3来处理`。
    3. 更多服务端机器按需添加Xml配置即可。


#### **步骤8：** 服务端配置

配置链接：http://{ip:port}/cat/s/config?op=serverConfigUpdate

`说明：这个只需要更新一次，配置是保存在mysql的数据库里面。`

- CAT节点的不同职责

    1.	控制台 - 提供给业务人员进行数据查看【默认所有的cat节点都可以作为控制台，不可配置】
    2.	消费机 - 实时接收业务数据，实时处理，提供实时分析报表【默认所有的cat节点都可以作为消费机，不可配置】
    3.	告警端 - 启动告警线程，进行规则匹配，发送告警（目前仅支持单点部署）【可以配置】
    4.	任务机 - 做一些离线的任务，合并天、周、月等报表 【可以配置】

- 本机模式

    本机模式可直接复制以下内容，并且修改127.0.0.1为实际的内网IP，然后点击提交。

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <server-config>
       <server id="default">
          <properties>
             <property name="local-mode" value="false"/>
             <property name="job-machine" value="false"/>
             <property name="send-machine" value="false"/>
             <property name="alarm-machine" value="false"/>
             <property name="hdfs-enabled" value="false"/>
             <property name="remote-servers" value="127.0.0.1:8080"/>  <!-- 本机模式这个IP替换为cat拿到的内网IP-->
          </properties>
          <storage  local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="7" local-logivew-storage-time="7">
            <hdfs id="logview" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="logview"/>
            <hdfs id="dump" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="dump"/>
            <hdfs id="remote" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="remote"/>
          </storage>
          <consumer>
             <long-config default-url-threshold="1000" default-sql-threshold="100" default-service-threshold="50">
                <domain name="cat" url-threshold="500" sql-threshold="500"/>
                <domain name="OpenPlatformWeb" url-threshold="100" sql-threshold="500"/>
             </long-config>
          </consumer>
       </server>
       <server id="127.0.0.1"> <!-- 本机模式这个IP替换为cat拿到的内网IP，此IP是看transaction报表下cat的自己上报的IP，用127.0.0.1是没用的。 -->
          <properties>
             <property name="job-machine" value="true"/>
             <property name="alarm-machine" value="true"/>
             <property name="send-machine" value="true"/>
          </properties>
       </server>
    </server-config>
    ```
    
- 集群模式

    线上做多集群部署，假设有10.1.1.1，10.1.1.2，10.1.1.3这三台机器
    
    1. 建议选取一台10.1.1.1 负责角色有控制台、告警端、任务机，建议配置域名访问CAT，就配置一台机器10.1.1.1一台机器挂在域名下面
    2. 10.1.1.2，10.1.1.3 负责消费机处理，这样能做到有效隔离，任务机、告警等问题不影响实时数据处理
    3. remote-servers 这个一定要配置正确，端口号为8080，这里面的remote-servers为10.1.1.1:8080,10.1.1.2:8080,10.1.1.3:8080，如果显示出问题服务端数据，一般是这里面的配置IP或者PORT有问题
    4. 完成以上步骤，重启下当前CAT节点
    
    配置的sample如下： id="default"是默认的配置信息，server id="10.1.1.1" 如下的配置是表示10.1.1.1这台服务器的节点配置覆盖default的配置信息，比如下面的job-machine，alarm-machine，send-machine为true。
    
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <server-config>
       <server id="default">
          <properties>
             <property name="local-mode" value="false"/>
             <property name="job-machine" value="false"/>
             <property name="send-machine" value="false"/>
             <property name="alarm-machine" value="false"/>
             <property name="hdfs-enabled" value="false"/>
             <property name="remote-servers" value="10.1.1.1:8080,10.1.1.2:8080,10.1.1.3:8080"/>
          </properties>
          <storage  local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="7" local-logivew-storage-time="7">
            <hdfs id="logview" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="logview"/>
            <hdfs id="dump" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="dump"/>
            <hdfs id="remote" max-size="128M" server-uri="hdfs://10.1.77.86/user/cat" base-dir="remote"/>
          </storage>
          <consumer>
             <long-config default-url-threshold="1000" default-sql-threshold="100" default-service-threshold="50">
                <domain name="cat" url-threshold="500" sql-threshold="500"/>
                <domain name="OpenPlatformWeb" url-threshold="100" sql-threshold="500"/>
             </long-config>
          </consumer>
       </server>
       <server id="10.1.1.1">
          <properties>
             <property name="job-machine" value="true"/>
             <property name="alarm-machine" value="true"/>
         <property name="send-machine" value="true"/>
          </properties>
       </server>
    </server-config>
    ```
    
 - 配置说明：
    
    server模型：代表一台机器的配置。如果id为default，代表默认配置；如果id为ip，代表该台服务器的配置
    
      * property local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;
      * property hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；
      * property job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为 false；
      * property alarm-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为 false；
      * property send-machine : 定义当前服务告警是否发送（当时为了解决测试环境开启告警线程，但是最后告警不通知，此配置后续会逐步去除，建议alarm-machine开启为true的时候，这个同步为true）
      
    storage模型: 定义数据存储配置信息
    
      * property local-report-storage-time : 定义本地报告存放时长，单位为（天）
      * property local-logivew-storage-time : 定义本地日志存放时长，单位为（天）
      * property local-base-dir : 定义本地数据存储目录
      * property hdfs : 定义HDFS配置信息，便于直接登录系统
      * property server-uri : 定义HDFS服务地址
      * property console : 定义服务控制台信息
      * property remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）
      * ldap : 定义LDAP配置信息（这个可以忽略）
      * ldapUrl : 定义LDAP服务地址（这个可以忽略）


#### **步骤9：** 重启不影响数据可用性

`单机模式部署可略过此步骤，线上环境使用建议配置。`

1. 请在tomcat重启之前调用当前tomcat的存储数据的链接 http://${ip}:8080/cat/r/home?op=checkpoint，重启之后数据会恢复。【注意重启时间在每小时的整点10-55分钟之间】
2. 线上部署时候，建议把此链接调用存放于tomcat的stop脚本中，这样不需要每次手工调用


#### **步骤10：** 本地开发环境运行CAT

1.	请参照上述步骤，进行如下配置：
  - 配置/data/appdatas/cat/client.xml文件
  - 配置/data/appdatas/cat/datasources.xml文件
  - 服务器配置 http://{ip:port}/cat/s/config?op=serverConfigUpdate （注意本地节点的角色，job-machine&alarm-machine都可以配置为true，以便于debug）
2.	根据ide的类型，在cat目录中执行 mvn eclipse:eclipse 或者 mvn idea:idea，此步骤会生成一些代码文件，直接导入到工程会发现找不到类
3.	如果ide是eclipse，将源码以普通项目到入eclipse中，注意不要以maven项目导入工程
4.	启动方式：

  - Intellij IDEA 推荐使用tomcat插件启动cat-home模块，application context设置为/cat。
  - Tomcat启动：打成war包，将war包部署在Tomcat后，启动Tomcat 
  - test case启动：运行com.dianping.cat.TestServer 这个类，即可启动cat服务器；注意：执行的是startWebApp()这个test case
  
