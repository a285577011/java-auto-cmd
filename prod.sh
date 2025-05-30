#APP_BASE=`pwd`
#nohup java -server  -jar $APP_BASE/gameServer.jar > nohup.out 2>&1 &

#!/bin/sh
#gameserver.sh
#To start or stop gameserver.

#base dir of the application
APP_BASE=`pwd`
echo $APP_BASE


JDK_OPTS="-Xms2048m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$APP_BASE/logs/outmem.log -Dlog4j2.formatMsgNoLookups=true"

DK_OPTS=$JDK_OPTS" -XX:SurvivorRatio=1"

JDK_OPTS=$JDK_OPTS" -XX:+UseParNewGC"

JDK_OPTS=$JDK_OPTS" -XX:+UseConcMarkSweepGC"

JDK_OPTS=$JDK_OPTS" -XX:CMSFullGCsBeforeCompaction=5"

JDK_OPTS=$JDK_OPTS" -XX:+UseCMSCompactAtFullCollection"

JDK_OPTS=$JDK_OPTS" -XX:GCTimeRatio=19"

JDK_OPTS=$JDK_OPTS" -Xnoclassgc"

JDK_OPTS=$JDK_OPTS" -XX:CMSInitiatingOccupancyFraction=80"

JDK_OPTS=$JDK_OPTS" -XX:SoftRefLRUPolicyMSPerMB=1000"

JDK_OPTS=$JDK_OPTS" -XX:+PrintClassHistogram -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
JDK_OPTS=$JDK_OPTS" -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime"
JDK_OPTS=$JDK_OPTS" -XX:+PrintHeapAtGC -Xloggc:$APP_BASE/logs/gc.log"
JDK_OPTS=$JDK_OPTS" -XX:ErrorFile=$APP_BASE/logs/jvm_error%p.log"

#name of the application
APP_NAME=com.yanqu.road.server.GameServer
echo $APP_NAME


#name of the file record the process id of the application
PROCESS_ID_FILE=$APP_BASE/gameServer.pid
echo $PROCESS_ID_FILE

#process id of the application
PROCESS_ID=`cat $PROCESS_ID_FILE`
echo $PROCESS_ID

case "$1" in
    start)
        if [ "$PROCESS_ID" ]; then
            echo "PID file ($PROCESS_ID) found. Is $APP_NAME still running? Start aborted."
            exit 1
        fi
        
        /usr/bin/nohup  $JAVA_HOME/bin/java -server $JDK_OPTS -jar $APP_BASE/gameServer.jar  > nohup.out 2>&1 &
        echo $! > $PROCESS_ID_FILE
        
        echo "$APP_NAME started!"
    ;;
    stop)
        if [ "$PROCESS_ID" ]; then
            kill "$PROCESS_ID"
            rm -rf $PROCESS_ID_FILE
            echo "----------------------the $APP_NAME been killed------------------"
        else
            echo "----------------------the $APP_NAME is not running----------------"
        fi
    ;;
    *)
        echo "Usage: $0 start|stop"
    ;;
esac
exit 0
