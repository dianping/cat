#!/usr/bin/env bash
echo "initialize datasources.xml"
sed -i "s/MYSQL_URL/${MYSQL_URL}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_PORT/${MYSQL_PORT}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_USERNAME/${MYSQL_USERNAME}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_PASSWORD/${MYSQL_PASSWORD}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_SCHEMA/${MYSQL_SCHEMA}/g" /data/appdatas/cat/datasources.xml;
