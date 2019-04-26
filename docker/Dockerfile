FROM centos:centos6

ENV CATALINA_HOME=/usr/local/tomcat \
    HOME=/root \
    JAVA_HOME=/usr/lib/jvm/java \
    JAVA_VERSION=1.7.0 \
    TOMCAT_MAJOR_VERSION=7 \
    TOMCAT_MINOR_VERSION=7.0.79

COPY . ${HOME}/workspace/cat

RUN	set -ex \
  && echo running under user $(whoami) \
  && ls -la ${HOME}/workspace/cat \
# install utils
  && yum install -y yum-utils \
  && yum install -y tar wget \
# install Java7
  && yum install -y java-1.7.0-openjdk java-1.7.0-openjdk-devel \
  && mkdir -p "${CATALINA_HOME}" \
# install Tomcat7
  && cd ${CATALINA_HOME} \
  && wget -q https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}/v${TOMCAT_MINOR_VERSION}/bin/apache-tomcat-${TOMCAT_MINOR_VERSION}.tar.gz \
  && wget -qO- https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}/v${TOMCAT_MINOR_VERSION}/bin/apache-tomcat-${TOMCAT_MINOR_VERSION}.tar.gz.md5 | md5sum -c - \
  && tar zxf apache-tomcat-*.tar.gz \
  && mv apache-tomcat-${TOMCAT_MINOR_VERSION}/* . \
  && sed -i "s/port=\"8080\"/port=\"8080\"\ URIEncoding=\"utf-8\"/g" ${CATALINA_HOME}/conf/server.xml \
  && rm -rf apache-tomcat-* \
  && ls -la ${CATALINA_HOME} \
# build CAT
  && cd ${HOME}/workspace/cat \
  && ./mvnw clean install -DskipTests \
  && cp cat-home/target/*.war ${CATALINA_HOME}/webapps/cat.war \
  && ./mvnw clean \
# datasource config and script
  && mkdir -p /app /data/appdatas/cat \
  && cp docker/datasources.xml /data/appdatas/cat/datasources.xml \
  && cp docker/datasources.sh /datasources.sh \
  && chmod +x /datasources.sh \
# clean
  && rm -rf ${HOME}/.m2 \
  && yum -y remove --setopt=clean_requirements_on_remove=1 tar wget \
  && yum -y remove --setopt=clean_requirements_on_remove=1 java-1.7.0-openjdk-devel \
  && package-cleanup --leaves \
  && yum clean all

ENV PATH=${CATALINA_HOME}/bin:$PATH \
    JAVA_HOME=/usr/lib/jvm/jre

RUN set -ex \
# install mysql client
  && yum -y localinstall https://dev.mysql.com/get/mysql80-community-release-el6-1.noarch.rpm \
  && yum --disablerepo=mysql80-community --enablerepo=mysql57-community --showduplicates list mysql-community-client \
  && yum -y --disablerepo=mysql80-community --enablerepo=mysql57-community install mysql-community-client-5.7.22-1.el6.x86_64 \
  && package-cleanup --leaves \
  && yum clean all

CMD java -version
EXPOSE 8080
WORKDIR /app
