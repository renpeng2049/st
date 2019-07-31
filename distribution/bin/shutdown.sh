#!/usr/bin/env bash

export LC_ALL=zh_CN.UTF-8

DIR=`dirname "$0"`
DIR=`cd ${DIR}/..;pwd`
result=$(ps axw |grep "${DIR}/" | grep java | grep -v grep | wc -l)
if [ ${result} -ge 1 ];then
    echo -ne "$DIR will be killed "
    ps axw |grep "$DIR/" | grep java | grep -v grep | awk '{print($1)}'|xargs kill
    echo "killing......"
else
    echo "$DIR not exists !!!"
fi
#睡10秒
sleep 1
result=$(ps axw |grep "${DIR}/" | grep java | grep -v grep | wc -l)
if [ ${result} -ge 1 ];then
    echo -ne "$DIR will be killed force "
    ps axw |grep "$DIR/" | grep java | grep -v grep | awk '{print($1)}'|xargs kill -9
    echo "kill -9"
else
    echo "shutdown success"
fi
