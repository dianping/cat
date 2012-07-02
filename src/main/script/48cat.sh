#!/usr/bin/expect 
set timeout 30
spawn ssh -p 58422 root@10.1.6.48 killall -9 java &&cd /usr/local/tomcat/webapps && rm cat* -rf && cd /usr/local/tomcat/work && rm * -rf
expect "*?assword:*"
send "12qwaszx\r"

set timeout 30
spawn scp -P58422 /home/youyong/workspace/cat/cat-home/target/cat.war root@10.1.6.48:/usr/local/tomcat/webapps
expect "*?assword:*"
send "12qwaszx\r"
interact

set timeout 30
spawn ssh -p 58422 root@10.1.6.48  cd /usr/local/tomcat/bin && ./startup.sh
expect "*?assword:*"
send "12qwaszx\r"
interact 
