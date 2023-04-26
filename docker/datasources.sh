#!/bin/bash
echo "initialize datasources.xml"
echo "<?xml version=\"1.0\" encoding=\"utf-8\"?><data-sources><data-source id=\"cat\"><maximum-pool-size>3</maximum-pool-size><connection-timeout>1s</connection-timeout><idle-timeout>10m</idle-timeout><statement-cache-size>1000</statement-cache-size><properties><driver>com.mysql.jdbc.Driver</driver><url><![CDATA[jdbc:mysql://${MYSQL_URL}:${MYSQL_PORT}/${MYSQL_SCHEMA}]]></url><user>${MYSQL_USERNAME}</user><password>${MYSQL_PASSWD}</password><connectionProperties><![CDATA[useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&socketTimeout=120000]]></connectionProperties></properties></data-source></data-sources>" > /data/appdatas/cat/datasources.xml;
#echo "$(sed 's/MYSQL_URL/${MYSQL_URL}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
#echo "$(sed 's/MYSQL_PORT/${MYSQL_PORT}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
#echo "$(sed 's/MYSQL_USERNAME/${MYSQL_USERNAME}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
#echo "$(sed 's/MYSQL_PASSWD/${MYSQL_PASSWD}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
#echo "$(sed 's/MYSQL_SCHEMA/${MYSQL_SCHEMA}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
