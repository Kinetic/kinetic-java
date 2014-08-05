#! /usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

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

CLASSPATH=${CLASSPATH}:$BASE_DIR/kinetic-test/target/test-classes

echo "CLASSPATH=$CLASSPATH"

exec "$JAVA" -classpath "$CLASSPATH" com.seagate.kinetic.example.client.Ping "$@"
