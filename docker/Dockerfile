FROM tomcat:8.5.78-jdk8

MAINTAINER gyl "shiyindaxiaojie@gmail.com"

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS '-Xmx1024m -Xms1024m -Xmn512m'
ENV MYSQL_URL ''
ENV MYSQL_PORT '3306'
ENV MYSQL_USERNAME ''
ENV MYSQL_PASSWD ''
ENV MYSQL_SCHEMA 'cat'
ENV SERVER_URL '127.0.0.1'

COPY cat-home/target/cat-home.war /usr/local/tomcat/webapps/cat.war
COPY docker/tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY docker/client.xml /data/appdatas/cat/client.xml
COPY docker/client.sh client.sh
COPY docker/datasources.xml /data/appdatas/cat/datasources.xml
COPY docker/datasources.sh datasources.sh
RUN chmod +x datasources.sh \
    && chmod +x client.sh

CMD ["/bin/sh", "-c", "./datasources.sh", "./client.sh", "catalina.sh run"]
