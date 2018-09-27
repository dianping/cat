mkdir /usr/local/maven
cd /usr/local/maven

wget  http://mirror.bit.edu.cn/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz

tar -xvf  apache-maven-3.2.5-bin.tar.gz

#set environment
export M2_HOME="/usr/local/maven/apache-maven-3.2.5"
if ! grep "M2_HOME=/usr/local/maven/apache-maven-3.2.5" /etc/environment 
then
  echo "M2_HOME=/usr/local/maven/apache-maven-3.2.5" |tee -a /etc/environment 
  echo "PATH=$PATH:$M2_HOME/bin" | tee -a /etc/environment 
fi

#update environment
source /etc/environment  
echo "maven is installed !"
