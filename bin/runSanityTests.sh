#!/usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"
HOST=127.0.0.1
PORT=8123
SSL_PORT=8443
HOME=`cd ~; pwd`
DEBUG=false
TYPE=basic

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
   echo "Usage: sh runSanityTests.sh [-option]"
   echo "       [-option]"
   echo "            -help                 usage"
   echo "            -host                 drive or simulator device ip address, default is 127.0.0.1"
   echo "            -port                 connection port, default is 8123"
   echo "            -debug                debug swith, default is false"
   echo "            -type                 admin or basic or all, default is basic"
}


if [ $# -eq 0 ]; then
   echo "Miss parameters, please see -help"
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
    -debug)
      echo "Print debug \"$2\""
      DEBUG=$2
      shift
      ;;
    -type)
      echo "Print type \"$2\""
      TYPE=$2
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

if [ "$TYPE" == "basic" ]; then
    exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -Dkinetic.io.in=$DEBUG -Dkinetic.io.out=$DEBUG -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT com.seagate.kinetic.allTests.BasicAPISanityTestsRunner 1>$BASE_DIR/bin/basic.result 2>&1
elif [ "$TYPE" == "admin" ]; then
    exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -Dkinetic.io.in=$DEBUG -Dkinetic.io.out=$DEBUG -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT com.seagate.kinetic.allTests.AdminAPISanityTestsRunner 1>$BASE_DIR/bin/admin.result 2>&1
elif [ "$TYPE" == "all" ]; then
    `exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -Dkinetic.io.in=$DEBUG -Dkinetic.io.out=$DEBUG -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT com.seagate.kinetic.allTests.BasicAPISanityTestsRunner 1>$BASE_DIR/bin/basic.result 2>&1`
    `exec "$JAVA" -classpath "$CLASSPATH" -DRUN_NIO_TEST=true -Dkinetic.io.in=$DEBUG -Dkinetic.io.out=$DEBUG -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=$HOST -DKINETIC_PORT=$PORT com.seagate.kinetic.allTests.AdminAPISanityTestsRunner 1>$BASE_DIR/bin/admin.result 2>&1`
else
    echo "Invalid type"
    printUsage;
fi

exit 0