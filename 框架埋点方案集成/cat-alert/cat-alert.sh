# -*- coding: utf-8 -*-
# @Time    : 2018/1/27 上午11:23
# @Author  : 宜信致诚，徐岩华
# @File    : cat-alert.sh
# @Software: PyCharm

#!/bin/sh

function start()
{
    python cat-alert/main.py &
    if [ $? -eq 0 ]; then
        echo 'start ok!'
    fi
    echo 'see log use "tail -f debug.log"'
}

function stop()
{
    pid=`ps -ef |grep python | grep cat-alert|awk '{print $2}'`
    if [ ! $pid ] ; then
        echo 'cat-alert has stopped.'
    else
        kill -9 $pid
        if [ $? -eq 0 ]; then
            echo 'stop ok!'
        fi
    fi
}

# command error
if [ $# -lt 1 ] ; then
    echo "USAGE: $0 start [port]"
    echo "       $0 stop"
    echo "       $0 restart [port]"
    exit 1;
fi

# port get
port=8888
if [ $# -eq 2 ] ; then
    port=$2
fi

if [ $1 == 'start' ] ; then
    start $port
elif [ $1 == 'stop' ] ; then
    stop
elif [ $1 == 'restart' ] ; then
    stop
    start $port
fi

