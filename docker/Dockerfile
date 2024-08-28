FROM tomcat:8.5.78-jdk8

LABEL maintainer="梦想歌 <shiyindaxiaojie@gmail.com>"
LABEL version="3.4.1"

ENV HOME "/data"
ENV TZ "Asia/Shanghai"
ENV LANG "C.UTF-8"
ENV XMS "1g"
ENV XMX "1g"
ENV XSS "256k"
ENV GC_MODE "G1"
ENV USE_GC_LOG "Y"
ENV USE_HEAP_DUMP "Y"
ENV USE_LARGE_PAGES "N"
ENV MYSQL_URL ""
ENV MYSQL_PORT "3306"
ENV MYSQL_USERNAME ""
ENV MYSQL_PASSWORD ""
ENV MYSQL_SCHEMA "cat"
ENV SERVER_URL "127.0.0.1"

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
