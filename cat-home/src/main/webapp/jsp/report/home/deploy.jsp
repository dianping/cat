<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">生产环境部署</h4>
<p>1、配置生产环境数据库，数据库脚本在资源文件 scrip/CatApplication.sql。</p>
<p>2、准备N台cat服务器，比如3台，ip为10.1.1.1，10.1.1.2，10.1.1.3。</p>
<p>3、在所有cat服务器上安装tomcat，启动端口默认设定为8080。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tomcat启动参数参考：-Xms20288m -Xmx20288m -XX:PermSize=256m -XX:MaxPermSize=256m -XX:NewSize=10144m -XX:MaxNewSize=10144m -XX:SurvivorRatio=10</p>
<p>4、确保所有cat客户端以及服务器对于/data目录具有读写权限。</p>
<p>5、配置所有客户端和服务端的配置文件client.xml，文件路径/data/appdatas/cat/client.xml。</p>
<xmp class="well">
	<config mode="client">
	    	<servers>
	                <server ip="10.1.1.1" port="2280" http-port="8080"/>
	                <server ip="10.1.1.2" port="2280" http-port="8080"/>
	                <server ip="10.1.1.3" port="2280" http-port="8080"/>
	    	</servers>
	</config>
</xmp>
<p>6、配置服务端的数据库配置datasources.xml，文件路径/data/appdatas/cat/datasources.xml,需要替换对应的线上配置。</p>
<xmp class="well">
<data-sources>
	<data-source id="cat">
		<maximum-pool-size>3</maximum-pool-size>
		<connection-timeout>1s</connection-timeout>
		<idle-timeout>10m</idle-timeout>
		<statement-cache-size>1000</statement-cache-size>
		<properties>
			<driver>com.mysql.jdbc.Driver</driver>
			<url><![CDATA[{jdbc.url}]]></url>
			<user>{jdbc.user}</user>
			<password>{jdbc.password}</password>
			<connectionProperties><![CDATA[useUnicode=true&autoReconnect=true]]></connectionProperties>
		</properties>
	</data-source>
</data-sources>
</xmp> 
<p>7、配置服务端的server.xml，文件路径/data/appdatas/cat/server.xml。</p>
<xmp class="well">
<!-- Configuration for production environment -->
<!-- Note: -->
<!-- 1. Set local-mode false to activate remote mode. -->
<!-- 2. If machine is job-machine, set job-machine true, you just need config only one machine. Job is offline for report aggreation, statistics report.-->
<!-- 3. If machine is alert-machine, set alert-machine true, you just need config only one machine. -->
<!-- 4. Cat can run without hdfs, you just config hdfs-machine false. If you have hdfs, you can config hdfs info for saving the logview info.  -->
<!-- 5. If you don't need hdfs, the logview will be stored in local disk. You can config max local-logivew-storage-time for cleaning up old logview, the unit is day. -->
<!-- 6. Please set hadoop environment accordingly. -->
<!-- 7. Please set ldap info for login the system. -->
<!-- 8. Please config remote-server if you have many cat servers. -->
<config local-mode="false" hdfs-machine="false" job-machine="false" alert-machine="false">
	<storage  local-base-dir="/data/appdatas/cat/bucket/" max-hdfs-storage-time="15" local-report-storage-time="7" local-logivew-storage-time="7">
		<hdfs id="logview" max-size="128M" server-uri="hdfs://${hdfs_path1}" base-dir="logview"/>
		<hdfs id="dump" max-size="128M" server-uri="hdfs://${hdfs_path2}" base-dir="dump"/>
		<hdfs id="remote" max-size="128M" server-uri="hdfs://${hdfs_path3}" base-dir="remote"/>
	</storage>
	<console default-domain="Cat" show-cat-domain="true">
		<remote-servers>10.1.1.1:8080,10.1.1.2:8080,10.1.1.3:8080</remote-servers>		
	</console>
	<ldap ldapUrl="ldap://${ldap_path1}"/>
</config>
</xmp>
<p>9、启动一台服务端10.1.1.1，修改服务端路由文件，url地址 http://10.1.1.1:8080/cat/s/config?op=routerConfigUpdate</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;需要用户名密码登陆，如果配置ldap即可直接登陆，或者用默认账号catadmin/catadmin登陆。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可以将10.1.1.1 部署为提供内部访问，并设置job-machine=true，alert-machine=true，让这台机器进行后续job以及告警处理，这些都可能影响到consumer性能。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;将10.1.1.2,10.1.1.3 处理全部监控请求，如果后续需要扩容，可以直接添加default-server的节点。</p>
<xmp class="well">
<?xml version="1.0" encoding="utf-8"?>
<router-config backup-server="10.1.1.1" backup-server-port="2280">
   <default-server id="10.1.1.2" port="2280" enable="true"/>
   <default-server id="10.1.1.3" port="2280" enable="true"/>
</router-config>
</xmp>
<p>10、客户端集成，请参考集成文档</p>