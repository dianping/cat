<head><meta http-equiv="Content-Type" content="text/html;charset=utf-8"/></head>
# 背景
本文描述了如何部署和维护CAT. 如果你有任何问题, 请联系CAT团队成员.

------------------------------------------------

# 部署
你需要增加这个文件:/data/appdatas/cat/server.xml

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

同时检查这几个地方:

* 在remote-servers内增加新机器的ip
* 检查 /data/appdatas/cat/cat.keytab 这个文件是否有效
* 检查 hdfs://10.1.1.169/user/cat     这个hdfs目录是否有效

--------------------------------------------------------

# 数据清理
 hdfs://10.1.1.169/user/cat/dump 下需要保留近3天的数据, 供分析报表使用. hdfs://10.1.1.169/user/cat/logview 下需要保留近1个月的数据, 供分析报表使用