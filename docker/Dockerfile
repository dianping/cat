FROM tomcat:8.5.78-jdk8

LABEL maintainer="梦想歌 <shiyindaxiaojie@gmail.com>"
LABEL version="3.3.1"

ENV TZ="Asia/Shanghai" \
    WORKDIR='/usr/local/tomcat/bin' \
    JVM_DEBUG='n' \
    JVM_XMS='1g' \
    JVM_XMX='1g' \
    JVM_XMN='512m' \
    JVM_MS='128m' \
    JVM_MMS='320m' \
    MYSQL_URL='' \
    MYSQL_PORT='3306' \
    MYSQL_USERNAME='' \
    MYSQL_PASSWORD='' \
    MYSQL_SCHEMA='cat' \
    SERVER_URL='127.0.0.1'

COPY cat-home/target/cat-home.war /usr/local/tomcat/webapps/cat.war
COPY docker/tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY docker/datasources.xml /data/appdatas/cat/datasources.xml
COPY docker/datasources.sh datasources.sh
COPY docker/client.xml /data/appdatas/cat/client.xml
COPY docker/client.sh client.sh
COPY docker/env.sh env.sh

RUN chmod +x datasources.sh \
    && chmod +x client.sh \
    && chmod +x env.sh

CMD ["/bin/sh", "-c", "./env.sh && ./datasources.sh && ./client.sh && catalina.sh run"]
