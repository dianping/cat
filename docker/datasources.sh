#!/usr/bin/env bash
echo "initialize datasources.xml"
echo "$(sed 's/MYSQL_URL/${MYSQL_URL}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
echo "$(sed 's/MYSQL_PORT/${MYSQL_PORT}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
echo "$(sed 's/MYSQL_USERNAME/${MYSQL_USERNAME}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
echo "$(sed 's/MYSQL_PASSWD/${MYSQL_PASSWD}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
echo "$(sed 's/MYSQL_SCHEMA/${MYSQL_SCHEMA}/g' /data/appdatas/cat/datasources.xml)" > /data/appdatas/cat/datasources.xml;
