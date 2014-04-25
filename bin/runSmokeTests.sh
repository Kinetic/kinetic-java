#!/usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"
HOST=127.0.0.1
PORT=8123
SSL_PORT=8443
HOME=`cd ~; pwd`
USE_SOCKET_LOG=false
LOG_SERVER_IP=127.0.0.1
LOG_SERVER_PORT=60123
LOG_FORMATTER=com.seagate.kinetic.socketlog.DefaultLogFormatter

JAVA=""
if [ "$JAVA_HOME" != "" ]; then
   JAVA=$JAVA_HOME/bin/java
else
   echo "JAVA_HOME must be set."
   exit 1
fi

#Set the classpath

if [ "$CLASSPATH" != "" ]; then
   CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
else
CLASSPATH=$JAVA_HOME/lib/tools.jar
fi

for f in $BASE_DIR/kinetic-test/target/*.jar; do
   CLASSPATH=${CLASSPATH}:$f
done

#echo "CLASSPATH=$CLASSPATH" 


printUsage(){
   echo "Usage: sh runSmokeTests.sh [-option]"
   echo "       [-option]"
   echo "            -help                 usage"
   echo "            -host                 drive or simulator device ip address, default is 127.0.0.1"
   echo "            -port                 connection port, default is 8123"
   echo "            -tlsport              ssl connection port, defalt is 8443"
   echo "            -home                 drive or simulator home, default is user home path"
   echo "            -usesocketlog         use socket log or not, default is false"
   echo "            -logserverip          when use socket log is true, log server's ip address, default is 127.0.0.1"
   echo "            -logserverport        when use socket log is true, log server's connection port, default is 60123"
   echo "            -logformatter         log formatter class designed for sending to log server, default is com.seagate.kinetic.socketlog.DefaultLogFormatter"
}


if [ $# -eq 0 ]; then
   exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -DRUN_SSL_TEST=true -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST com.seagate.kinetic.allTests.AllTestsRunner
   exit 0
fi


until [ $# -eq 0 ]
do
   case $1 in
   -help)
       printUsage;
       exit 0
       ;;
   -host)
      echo "Print host \"$2\""
      HOST=$2
      shift
      ;;
    -port)
      echo "Print port \"$2\""
      PORT=$2
      shift
      ;;
    -tlsport)
      echo "Print tls port \"$2\""
      SSL_PORT=$2
      shift
      ;;
    -home)
      echo "Print home \"$2\""
      HOME=$2
      shift
      ;;
    -usesocketlog)
      echo "Print usesocketlog \"$2\""
      USE_SOCKET_LOG=$2
      shift
      ;;
    -logserverip)
      echo "Print logserverip \"$2\""
      LOG_SERVER_IP=$2
      shift
      ;;
    -logserverport)
      echo "Print logserverport \"$2\""
      LOG_SERVER_PORT=$2
      shift
      ;;
    -logformatter)
      echo "Print logformatter \"$2\""
      LOG_FORMATTER=$2
      shift
      ;;
      
   *)
      echo "Invalid option : $1"
      printUsage;
      exit 1
      ;;
   esac
   shift
done

exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -DRUN_SSL_TEST=true -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT -DKINETIC_SSL_PORT=$SSL_PORT  -DKINETIC_HOME=$HOME -DUSE_SOCKET_LOGGER=$USE_SOCKET_LOG -DLOG_SOCKET_HOST=$LOG_SERVER_IP -DLOG_SOCKET_PORT=$LOG_SERVER_PORT -DKINETIC_LOG_FORMATTER=$LOG_FORMATTER com.seagate.kinetic.allTests.AllTestsRunner

exit 0
