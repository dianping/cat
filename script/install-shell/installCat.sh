yum -y install git
git clone https://github.com/dianping/cat.git
chmod -R 777 /cat/
cd  cat/
mvn clean install -DskipTests

mkdir -p /data/appdatas/cat
mkdir -p /data/applogs/cat
chmod -R 777 /data/


echo "mysql_jdbcUrl=jdbc:mysql://127.0.0.1:3306" | tee -a /etc/environment 
echo "mysql_username=root" | tee -a /etc/environment 
echo "mysql_password=123456" | tee -a /etc/environment 
  #update environment
source /etc/environment 

cd /home/$LOGNAME
cd  cat/
mvn cat:install

#cd cat-home/
#mvn jetty:run








