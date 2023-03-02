FROM tomcat:8.5.78-jdk8

ENV TZ=Asia/Shanghai
COPY cat-home/target/cat-home.war /usr/local/tomcat/webapps/cat.war
COPY docker/client.xml /data/appdatas/cat/client.xml
COPY docker/client.sh client.sh
COPY docker/datasources.xml /data/appdatas/cat/datasources.xml
COPY docker/datasources.sh datasources.sh
RUN sed -i "s/port=\"8080\"/port=\"8080\"\ URIEncoding=\"utf-8\"/g" $CATALINA_HOME/conf/server.xml  \
    && chmod +x datasources.sh \
    && chmod +x client.sh

CMD ["/bin/sh", "-c", "./datasources.sh", "./client.sh", "catalina.sh run"]
