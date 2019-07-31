#!/usr/bin/env bash

export LC_ALL=zh_CN.UTF-8
export LANG=zh_CN.UTF-8
export JAVA_HOME=$(echo `which java` | sed 's/bin\/java//g')
umask 022

echo "Begin to startup:"
DIR=`dirname "$0"`
APP_HOME=`cd ${DIR}/../;pwd`
APP_LOG_HOME=${APP_HOME}/logs

result=$(ps axw |grep "${APP_HOME}/" | grep java | wc -l)
if [ ${result} -ge 1 ];then
    echo -ne "$APP_HOME process is exists"
    exit 1
fi

# memory
JAVA_OPTS="-server -Xms256m -Xmx1g -Xss256k -XX:MaxDirectMemorySize=2g"
if [[ "DEBUG" = $1 ]];then
# debug setting
JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=2345"
fi
# performance setting
JAVA_OPTS="${JAVA_OPTS} -XX:-UseBiasedLocking -XX:-UseCounterDecay -XX:AutoBoxCacheMax=20000"

#G1 GC
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
#GC LOG
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
JAVA_OPTS="${JAVA_OPTS} -Xloggc:${APP_LOG_HOME}/gc.log"
# jvm exception log
JAVA_OPTS="${JAVA_OPTS} -XX:ErrorFile=${APP_LOG_HOME}/hs_err_%p.log"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${APP_LOG_HOME}"
JAVA_OPTS="${JAVA_OPTS} -XX:OnError=\"${JAVA_HOME}/bin/jstack %p > ${APP_LOG_HOME}/java_error.log\""
# other setting
JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8 -Dlog.home=${APP_LOG_HOME} -Dio.netty.leakDetectionLevel=advanced"

CLASS_PATH="-classpath ${APP_HOME}/conf:${APP_HOME}/lib/*"
JAVA_MAIN_CLASS="com.soyoung.st.Application"

echo ${JAVA_OPTS}
echo "CLASSPATH:${CLASS_PATH}"

eval "${JAVA_HOME}/bin/java ${JAVA_OPTS} ${CLASS_PATH} ${JAVA_MAIN_CLASS} > ${APP_LOG_HOME}/out.log  2>&1 &"
echo "server process started"

echo "Begin to add this script to crontab"

PROGRAM=${APP_HOME}/bin/startup.sh
CRONTAB_CMD="*/3 * * * * source /etc/profile;sh ${PROGRAM} > /dev/null 2>&1 &" 
(crontab -l 2>/dev/null | grep -Fv ${PROGRAM}; echo "${CRONTAB_CMD}") | crontab -

crontab -l | grep ${PROGRAM}
echo "add crontab success"
