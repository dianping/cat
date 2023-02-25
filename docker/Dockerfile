FROM maven:3.8.4-openjdk-8 as mavenrepo

WORKDIR /app
COPY cat-alarm cat-alarm
COPY cat-consumer cat-consumer
COPY cat-hadoop cat-hadoop
COPY cat-client cat-client
COPY cat-core cat-core
COPY cat-home cat-home
COPY pom.xml pom.xml
RUN mvn clean package -DskipTests

FROM tomcat:8.5.41-jre8-alpine
ENV TZ=Asia/Shanghai
COPY --from=mavenrepo /app/cat-home/target/cat-home.war /usr/local/tomcat/webapps/cat.war
COPY docker/datasources.xml /data/appdatas/cat/datasources.xml
COPY docker/datasources.sh datasources.sh
RUN sed -i "s/port=\"8080\"/port=\"8080\"\ URIEncoding=\"utf-8\"/g" $CATALINA_HOME/conf/server.xml && chmod +x datasources.sh

CMD ["/bin/sh", "-c", "./datasources.sh && catalina.sh run"]