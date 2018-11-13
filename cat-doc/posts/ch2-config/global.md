## 全局系统配置

### 服务端配置

通过服务端配置，配置每台CAT服务器的职责。

配置的sample如下： id="default"是默认的配置信息，server id="10.1.1.1" 如下的配置是表示10.1.1.1这台服务器的节点配置覆盖default的配置信息，比如下面的job-machine，alarm-machine，send-machine为true。
[注意这个IP为cat拿到的内网IP，如果你cat部署本地，此IP是看transaction报表下cat的自己上报的IP，用127.0.0.1是没用的。]

```
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

配置说明：

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


### 消息采样配置

通过消息采样配置，配置每个项目的消息采样率。客户端会定期下拉改配置，根据采样率来采样上报。

采样率不会影响Transaction、Event的汇总指标。当某个项目数据量较大时，建议按需配置采样率。

#### 配置示例

	<sample-config>
       <domain id="cat-web" sample="1.0"/>
       <domain id="cat-service" sample="0.1"/>
    </sample-config>
	
#### 配置说明：

  * domain id属性：项目名
  * sample：项目的采样率


### 客户端路由

通过客户端路由，配置客户端实际上报的服务器地址。客户端会定期下拉改配置。

#### 配置示例

	<router-config backup-server="127.0.0.1" backup-server-port="2280">
       <default-server id="127.0.0.1" weight="1.0" port="2280" enable="true"/>
       <network-policy id="default" title="??" block="false" server-group="default_group">
       </network-policy>
       <server-group id="default_group" title="default-group">
          <group-server id="127.0.0.1"/>
       </server-group>
       <domain id="cat">
          <group id="default">
             <server id="127.0.0.1" port="2280" weight="1.0"/>
          </group>
       </domain>
    </router-config>
	
#### 配置说明：

  * backup-server属性：设置为当前服务器对外IP地址，端口固定为2280
  * default-server属性：定义可跳转的路由地址，可以设置多个。default-server的id属性配置可路由的cat-home服务IP地址，端口固定为2280;若需要禁用路由地址，可把enable设置为false

