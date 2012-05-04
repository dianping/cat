# 服务端配置
服务端配置文件位于/data/appdatas/cat/server.xml 

~~~~.xml
<config local-mode="false">
        <storage local-base-dir="/data/appdatas/cat/bucket">
                <hdfs id="logview" max-size="128M" server-uri="hdfs://10.1.1.169/user/cat" base-dir="logview"/>
                <hdfs id="dump" max-size="128M" server-uri="hdfs://10.1.1.169/user/cat" base-dir="dump"/>
                <properties>
                        <property name="hadoop.security.authentication" value="kerberos"/>
                        <property name="dfs.cat.kerberos.principal" value="cat@DIANPING.COM"/>
                        <property name="dfs.cat.keytab.file" value="/data/appdatas/cat/cat.keytab"/>
                        <property name="java.security.krb5.kdc" value="10.1.1.170"/>
                        <property name="dfs.namenode.kerberos.principal" value="hadoop/10.1.1.169@DIANPING.COM"/>
                        <property name="java.security.krb5.realm" value="DIANPING.COM"/>
                </properties>
        </storage>
        <consumer>
                <long-url default-threshold="1000">
                        <domain name="Cat" threshold="500"/>
                </long-url>
        </consumer>
        <console default-domain="MobileApi" show-cat-domain="true">
             <remote-servers>10.1.6.48:8080,10.1.6.37:8080</remote-servers>
        </console>
</config>
~~~~



# 客户端配置
/data/appdatas/cat/client.xml 


