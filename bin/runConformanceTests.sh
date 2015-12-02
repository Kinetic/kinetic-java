#!/usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"
HOST=127.0.0.1
PORT=8123
SSL_PORT=8443
HOME=`cd ~; pwd`
USE_SSL=false
ISE=false

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
   echo "Usage: sh runConformanceTests.sh [-option]"
   echo "       [-option]"
   echo "            -help                 usage"
   echo "            -host                 drive or simulator device ip address, default is 127.0.0.1"
   echo "            -port                 connection port, default is 8123"
   echo "            -tlsport              ssl connection port, defalt is 8443"
   echo "            -home                 drive or simulator home, default is user home path"
   echo "            -ssl                  true or false, default is false, run test without ssl connecttion"
   echo "            -ise                  true or false, default is false, use ise or not before test"
}


if [ $# -eq 0 ]; then
   exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_NIO_TEST=true -DRUN_SSL_TEST=${USE_SSL} -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DISE=$ISE com.seagate.kinetic.allTests.ConformanceTestRunner
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
    -ssl)
      echo "Print ssl \"$2\""
      USE_SSL=$2
      shift
      ;;
    -ise)
      echo "Print ssl \"$2\""
      ISE=$2
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

exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_NIO_TEST=true -DRUN_SSL_TEST=${USE_SSL} -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT -DKINETIC_SSL_PORT=$SSL_PORT  -DKINETIC_HOME=$HOME -DISE=$ISE com.seagate.kinetic.allTests.ConformanceTestRunner

exit 0
