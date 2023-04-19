FROM tomcat:8.5.78-jdk8

ENV TZ=Asia/Shanghai
COPY cat-home/target/cat-home.war /usr/local/tomcat/webapps/cat.war
COPY tomcat-server.xml /usr/local/tomcat/conf/server.xml
COPY client.xml /data/appdatas/cat/client.xml
COPY client.sh client.sh
COPY datasources.xml /data/appdatas/cat/datasources.xml
COPY datasources.sh datasources.sh
RUN chmod +x datasources.sh \
    && chmod +x client.sh

CMD ["/bin/sh", "-c", "./datasources.sh", "./client.sh", "catalina.sh run"]
