#!/usr/bin/env bash

MYSQL_CMD="mysql -h ${MYSQL_URL:-cat-mysql} -P ${MYSQL_PORT:-3306} -u ${MYSQL_USERNAME:-root}"
if [[ -n "${MYSQL_PASSWD}" ]]; then
    MYSQL_CMD="${MYSQL_CMD} -p${MYSQL_PASSWD}"
else
    MYSQL_CMD="${MYSQL_CMD} --password=\"\""
fi

${MYSQL_CMD} -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_SCHEMA:-cat}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;"
${MYSQL_CMD} -D ${MYSQL_SCHEMA:-cat} < ${HOME}/workspace/cat/script/CatApplication.sql

sed -i "s/MYSQL_URL/${MYSQL_URL:-cat-mysql}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_PORT/${MYSQL_PORT:-3306}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_USERNAME/${MYSQL_USERNAME:-root}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_PASSWD/${MYSQL_PASSWD:-}/g" /data/appdatas/cat/datasources.xml;
sed -i "s/MYSQL_SCHEMA/${MYSQL_SCHEMA:-cat}/g" /data/appdatas/cat/datasources.xml;
