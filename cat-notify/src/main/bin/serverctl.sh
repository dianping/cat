#!/bin/bash
### ====================================================================== ###
##                                                                          ##
##   Sync-Server Startup Script                                             ##
##                                                                          ##
### ====================================================================== ###

PROG_NAME=$0
ACTION=$1
CONF_FILE=cat-notify.xml

usage() {
    echo "Usage: $PROG_NAME {start|stop|restart} {configPath}"
    exit 1;
}

if [ $# -lt 1 ]; then
    usage
fi

if [ `whoami` == "root" ]; then
  echo DO NOT use root user to launch me.
  exit 1;
fi

#SET BAS DIR
cd `dirname $0`/..
BASE_DIR="`pwd`"
PID_DIR=$BASE_DIR/pid
START_LOG_PATH=$BASE_DIR/logs
START_LOG=$START_LOG_PATH/default.log

# Replicator manager class.  
MGR_CLASS=com.dianping.cat.notify.CatNotifyServer

#export JAVA_HOME=${serverctl.java.home}
#export PATH=$JAVA_HOME/bin:$PATH
export NLS_LANG=AMERICAN_AMERICA.ZHS16GBK
export LANG=zh_CN.GB18030

JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

JAVA_OPTS="$JAVA_OPTS -server "
JAVA_OPTS="$JAVA_OPTS -Xms512M "
#JAVA_OPTS="$JAVA_OPTS -Xmx512M "
JAVA_OPTS="$JAVA_OPTS -Xmx512M -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.PollSelectorProvider"
JAVA_OPTS="$JAVA_OPTS -XX:PermSize=40m "
JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=40m "
JAVA_OPTS="$JAVA_OPTS -Xss1m "
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError "
JAVA_OPTS="$JAVA_OPTS -XX:+UseParallelOldGC "
JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC "
JAVA_OPTS="$JAVA_OPTS -Xloggc:$START_LOG_PATH/gc.log "
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails "
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDateStamps"

JAVA_OPTS="$JAVA_OPTS -Dspring.context=$CONF_FILE"
JAVA_OPTS="$JAVA_OPTS -Dlog4j.configuration=file:$BASE_DIR/conf/log4j.xml"
JAVA_OPTS="$JAVA_OPTS -Dconfiguration=$BASE_DIR/conf/config.properties"

#for debug
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"
export JAVA_OPTS

if [ ! -f "$JAVA_HOME/bin/java" ]; then
  echo "PLEASE SET JAVA_HOME"
  exit 1;
fi

CLASSPATH=.:$BASE_DIR/conf

for jar in `ls $BASE_DIR/lib/*.jar`
do
    CLASSPATH="$CLASSPATH:""$jar"
done

if [ ! -d "$PID_DIR" ]; then
	 mkdir "$PID_DIR"
fi
PID_FILE_NAME=`echo "$CONF_FILE"|/usr/bin/md5sum|awk '{print $1}'`.pid
PID_FILE="$PID_DIR/$PID_FILE_NAME"
echo " PID FILE IS $PID_FILE"

if [ ! -d "$START_LOG_PATH" ]; then 
	mkdir "$START_LOG_PATH" 
fi 

start()
{
	echo "========================================================================="
	echo ""
	echo "  DataCenter-Server Startup Environment"
	echo ""
	echo "  BASE_DIR: $BASE_DIR"
	echo ""
	echo "  JAVA_HOME: $JAVA_HOME"
	echo ""
	echo "  JAVA_VERSION: `${JAVA_HOME}/bin/java -version`"
	echo ""
	echo "  JAVA_OPTS: $JAVA_OPTS"
	echo ""
   	echo "  CLASSPATH: $CLASSPATH"
	echo ""
	echo "========================================================================="
	echo ""
	#check Replicator Already Running
	if [ -f "$PID_FILE" ]; then
		PID_NUM=`cat $PID_FILE`
		if [ "" != " $PID_NUM" ]; then
			RUN_PID=`ps aux|grep -v "grep"|grep -v "$PROG_NAME"|grep $PID_NUM|sed -n '1P'|awk '{print $2}'`
			if [ "" != "$RUN_PID" ]; then
				echo "$PROG_NAME Already Running..............!"
				exit 1;
			fi
		fi   
	fi
	#Start Java Process
	$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $MGR_CLASS > $START_LOG 2>&1 &

	#write Process Pid To File
	echo $!>$PID_FILE
}

stop()
{
    #if [  -f "$START_LOG" ]; then  
	#mv -f $START_LOG "$START_LOG.`date '+%Y%m%d%H%M%S'`" 
    #fi  

    if [ -f "$PID_FILE" ]; then
	PID_NUM=`cat $PID_FILE`
	if [ "" != "$PID_NUM" ]; then
	    kill -9 $PID_NUM
	   echo "kill pid is: $PID_NUM"
	fi
    fi  
}

case "$ACTION" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        start
    ;;
    *)
        usage
    ;;
esac
