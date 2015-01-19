mv cat/cat-home/target/cat-alpha-1.2.2.war /usr/local/tomcat/apache-tomcat-7.0.57/webapps/cat.war
chown cat:cat /usr/local/tomcat/
su cat
nohup /usr/local/tomcat/apache-tomcat-7.0.57/bin/startup.sh

