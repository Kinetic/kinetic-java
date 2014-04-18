#! /usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PORT=60123


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
   echo "Usage: sh startLogServer.sh [-option]"
   echo "       [-option]"
   echo "            -help                 usage"
   echo "            -port                 log socket server connection port, default is 60123"
}

until [ $# -eq 0 ]
do
   case $1 in
   -help)
       printUsage;
       exit 0
       ;;
    -port)
      #echo "Print port \"$2\""
      PORT=$2
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

exec "$JAVA" -classpath "$CLASSPATH" com.seagate.kinetic.socketlog.LogServer $PORT "$@"

