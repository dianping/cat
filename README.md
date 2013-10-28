CAT
===

Central Application Tracking [![Build Status](https://travis-ci.org/dianping/cat.png?branch=biz)](https://travis-ci.org/dianping/cat)

#####1、Please install mysql server
#####2、excute the sql script in  'script/Cat.sql'
#####3、mkdir /data/appdatas/cat, make sure he has read and write permissions 
#####4、mkdir /data/applogs/cat , make sure he has read and write permissions
#####5、copy 'client.xml datasources.xml server.xml' to /data/appdatas/cat
#####6、modify the cat datatabse connection infomation, use your own cat connection
#####7、mvn eclipse:eclipse
#####8、import the workspace to eclipse
#####9、run testcase ‘com.dianping.cat.TestServer’