## 全局系统配置

### 告警策略

告警策略：配置某种类型、某个项目、某个错误级别，对应的告警发送渠道，以及暂停时间。

#### 配置示例

	<alert-policy>
		<type id="Business">
              <group id="default">
                 <level id="error" send="mail,weixin" suspendMinute="5"/>
                 <level id="warning" send="mail,weixin" suspendMinute="5"/>
              </group>
              <group id="demo-project1">
                 <level id="error" send="mail,weixin,sms" suspendMinute="5"/>
                 <level id="warning" send="mail,weixin,sms" suspendMinute="5"/>
              </group>
        </type>
	</alert-policy>
	
#### 配置说明：

  * type：告警的类型，可选：Transaction、Event、Business
  * group id属性：group可以为default，代表默认策略；也可以为项目名，代表某个项目的策略
  * level id属性：错误级别，分为warning-警告、error-错误
  * level send属性：告警渠道，分为mail-邮箱、weixin-微信、sms-短信
  * level suspendMinute属性：连续告警的暂停时间
  
  
### 默认告警人

某个告警类型的告警信息，均会发给默认告警人。默认告警人可以用于测试。

#### 配置示例

	<alert-config>
       <receiver id="Transaction" enable="true">
          <email>testUser1@test.com</email>
          <phone>12345678901</phone>
          <phone>12345678902</phone>
       </receiver>
    </alert-config>
	
#### 配置说明：

  * receiver id属性：告警的类型，可选：Transaction、Event、Business
  * receiver enable属性：是否开启告警；如果为false，此类别的告警不会发出
  * email：默认邮件告警人
  * phone：默认短信告警人


### 告警服务端

告警发送中心的Http接口。由于并不是所有告警中心都提供Http，该模块请按需定制。


### 服务端配置

某个告警类型的告警信息，均会发给默认告警人。默认告警人可以用于测试。

#### 配置示例

	<?xml version="1.0" encoding="utf-8"?>
    <server-config>
       <server id="default">
          <properties>
             <property name="local-mode" value="false"/>
             <property name="job-machine" value="false"/>
             <property name="send-machine" value="false"/>
             <property name="alarm-machine" value="false"/>
             <property name="hdfs-enabled" value="false"/>
             <property name="remote-servers" value="127.0.0.1:8080"/>
          </properties>
          <storage local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="2" local-logivew-storage-time="1" har-mode="true" upload-thread="5">
             <hdfs id="dump" max-size="128M" server-uri="hdfs://127.0.0.1/" base-dir="/user/cat/dump"/>
             <harfs id="dump" max-size="128M" server-uri="har://127.0.0.1/" base-dir="/user/cat/dump"/>
             <properties>
                <property name="hadoop.security.authentication" value="false"/>
                <property name="dfs.namenode.kerberos.principal" value="hadoop/dev80.hadoop@testserver.com"/>
                <property name="dfs.cat.kerberos.principal" value="cat@testserver.com"/>
                <property name="dfs.cat.keytab.file" value="/data/appdatas/cat/cat.keytab"/>
                <property name="java.security.krb5.realm" value="value1"/>
                <property name="java.security.krb5.kdc" value="value2"/>
             </properties>
          </storage>
          <consumer>
             <long-config default-url-threshold="1000" default-sql-threshold="100" default-service-threshold="50">
                <domain name="cat" url-threshold="500" sql-threshold="500"/>
                <domain name="OpenPlatformWeb" url-threshold="100" sql-threshold="500"/>
             </long-config>
          </consumer>
       </server>
       <server id="127.0.0.1">
          <properties>
             <property name="job-machine" value="true"/>
             <property name="send-machine" value="true"/>
             <property name="alarm-machine" value="true"/>
          </properties>
       </server>
    </server-config>

	
#### 配置说明：

server模型：代表一台机器的配置。如果id为default，代表默认配置

  * property local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;
  * property hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；
  * property job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为 false；
  * property alert-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为 false；

storage模型: 定义数据存储配置信息

  * property local-report-storage-time : 定义本地报告存放时长，单位为（天）
  * property local-logivew-storage-time : 定义本地日志存放时长，单位为（天）
  * property local-base-dir : 定义本地数据存储目录
  * property hdfs : 定义HDFS配置信息，便于直接登录系统
  * property server-uri : 定义HDFS服务地址
  * property console : 定义服务控制台信息
  * property remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）


### 消息采样配置

项目的消息采样率。客户端会定期下拉改配置。

#### 配置示例

	<sample-config>
       <domain id="cat" sample="1.0"/>
       <domain id="javacat" sample="0.1"/>
    </sample-config>
	
#### 配置说明：

  * domain id属性：项目名
  * sample：项目的采样率


### 客户端路由

客户端实际上报的服务器地址。客户端会定期下拉改配置。

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

