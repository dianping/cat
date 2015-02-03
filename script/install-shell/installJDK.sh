mkdir /usr/local/java
cd /usr/local/java

#download jdk 1.7.67
#wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/7u67-b01/jdk-7u67-linux-x64.tar.gz
wget --no-check-certificate --no-cookies --header "Cookie: s_nr=1392900709523; ORA_WWW_PERSONALIZE=v:1~i:6~r:6~g:APAC~l:en~cs:NOT_FOUND~cn:scut; ORASSO_AUTH_HINT=v1.0~20140322121132; ORA_UCM_INFO=3~xxxx21212xxxx~xxxx~xxxx~xxxx@163.com; s_cc=true; oraclelicense=accept-securebackup-cookie; gpw_e24=http%3A%2F%2Fwww.oracle.com%2Ftechnetwork%2Fjava%2Fjavase%2Fdownloads%2Fjava-archive-downloads-javase6-419409.html%23jdk-6u45-oth-JPR; s_sq=%5B%5BB%5D%5D;" http://download.oracle.com/otn-pub/java/jdk/6u45-b06/jdk-6u45-linux-x64.bin

#extract jdk
#tar -xvf jdk-7u67-linux-x64.tar.gz
mv jdk-6u45-linux-* jdk-6u45-linux-x64.bin
chmod +x jdk-6u45-linux-x64.bin
./jdk-6u45-linux-x64.bin 

#set environment
export JAVA_HOME="/usr/local/java/jdk1.6.0_45"
if ! grep "JAVA_HOME=/usr/local/java/jdk1.6.0_45" /etc/environment 
then
  echo "JAVA_HOME=/usr/local/java/jdk1.6.0_45" | tee -a /etc/environment 
  echo "PATH=$PATH:$JAVA_HOME/bin" | tee -a /etc/environment 
  echo "CLASSPATH=.:$JAVA_HOME/lib" | tee -a /etc/environment 
fi

#update environment
source /etc/environment  
echo "jdk is installed !"
