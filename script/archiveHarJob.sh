#!/bin/bash
HADOOP_PATH=/usr/local/hadoop/hadoop-release/bin 

#Archive hdfs to har
DATE_DIR=`date --date='yesterday' +'%Y%m%d'`
DATE_HOUR_DIR=`date +'%H'`
TARGET_PATH=/user/cat/dump/${DATE_DIR}
ARCHIVE_PATH=${TARGET_PATH}/${DATE_HOUR_DIR}

#Prune history data
DELETE_DURATION=15
PRUNE_DATE=`date --date="${DELETE_DURATION} days ago" +%Y%m%d`

echo "PRINT> Deleting files ${DELETE_DURATION} days ago: ${PRUNE_DATE}*"
${HADOOP_PATH}/hadoop fs -rm -R /user/cat/dump/${PRUNE_DATE}*

echo "PRINT> Archiving ${ARCHIVE_PATH} ..."
${HADOOP_PATH}/hadoop archive -archiveName ${DATE_HOUR_DIR}.har -p ${ARCHIVE_PATH} ${TARGET_PATH}

if [ $? -eq 0 ];then
        ${HADOOP_PATH}/hadoop fs -ls ${ARCHIVE_PATH}.har/part*
        if [ $? -eq 0 ];then
                echo "PRINT> Deleting source path ${ARCHIVE_PATH} ..."
                ${HADOOP_PATH}/hadoop fs -rm -R ${ARCHIVE_PATH}
        fi
else
        echo "PRINT> Faild to archive ${ARCHIVE_PATH}"
fi

