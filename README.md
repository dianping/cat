CAT [![Build Status](https://travis-ci.org/dianping/cat.png?branch=biz)](https://travis-ci.org/dianping/cat)
===

Central Application Tracking

#####1、安装Mysql服务器
#####2、【可选】配置Hadoop集群服务器
#####3、启动命令行，并将当前工作目录移动到cat目录
#####4、导入mysql的脚本文件，文件在'script/Cat.sql'
#####5、在本地磁盘中创建两个目录/data/appdatas/cat，以及/data/applogs/cat
		mkdir -p /data/appdatas/cat 
		mkdir -p /data/applogs/cat 
#####6、确保运行程序对上面两个目录拥有读写权限,比如用cat用户启动程序
        chown cat:cat /data/appdatas/cat -R
        chown cat:cat /data/applogs/cat -R
#####7、确保系统的临时目录程序拥有读写权限
#####8、拷贝script文件夹下的'client.xml datasources.xml server.xml'到/data/appdatas/cat目录下
		cp script/*.xml /data/appdatas/cat 
#####9、修改/data/appdatas/cat/datasource.xml中cat数据源的基本信息，使用您自己的数据库链接信息
#####10、【可选】如果你安装了hadoop集群，请配置/data/appdatas/cat/server.xml中对应hadoop信息，如果没有hadoop集群，server.xml中localmode必须为true
#####11、用maven构建项目
        mvn eclipse:eclipse
#####12、导入项目到eclipse中，并运行testcase启动项目 ‘com.dianping.cat.TestServer’