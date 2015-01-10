#!/usr/bin/env bash

BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"
LOG_DIR="kinetic_log"
SSL_PORT=8443
PORT=8123
HOME=`cd ~; pwd`
NODES=127.0.0.1
IFS_DEFAULT=##

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

printUsage(){
   echo "Usage: sh multiDrivesTest.sh [-option]"
   echo "       [-option]"
   echo "            -help                   usage"
   echo "            -iplist                 ip address for all drives. For instance, 192.168.2.11,192.168.2.12,192.168.2.13 or 192.168.2.11~192.168.2.13"
}

until [ $# -eq 0 ]
do
   case $1 in
   -help)
       printUsage;
       exit 0
       ;;
    -iplist)
      echo "Print iplist \"$2\""
      NODES=$2
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

mkdir -p ${LOG_DIR}

total_nodes=0
IFS=","
node_arr=($NODES)
for node in "${node_arr[@]}"
do
    if [[ $node == *~* ]]
    then
        IFS="~"
        node_ip_from_and_to=($node)
        node_ip_from=${node_ip_from_and_to[0]}
        node_ip_to=${node_ip_from_and_to[1]}
        IFS="."
        node_ip_from_as_array=(${node_ip_from})
        node_ip_to_as_array=(${node_ip_to})
        node_ip_from_last_byte=${node_ip_from_as_array[3]}
        node_ip_to_last_byte=${node_ip_to_as_array[3]}
        curr=${node_ip_from_last_byte}
        IFS=${IFS_DEFAULT}
        while [ $curr -le ${node_ip_to_last_byte} ]
        do
           node_ip=${node_ip_from_as_array[0]}.${node_ip_from_as_array[1]}.${node_ip_from_as_array[2]}.$curr
           echo "start test ${node_ip}..."
           exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_SSL_TEST=true -DKINETIC_HOST=${node_ip} com.seagate.kinetic.simulator.client.sanity.BasicAPISanityExample 1>${LOG_DIR}/${node_ip}.log 2>&1 & 
           exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_SSL_TEST=true -DKINETIC_HOST=${node_ip} com.seagate.kinetic.simulator.client.sanity.AdminAPISanityExample 1>${LOG_DIR}/${node_ip}.log 2>&1 & 
           total_nodes=$(( total_nodes + 1 )) 
           curr=$(( curr + 1 )) 
        done
    else
        node_ip=${node}
        echo "start test ${node_ip}..."
        exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_SSL_TEST=true -DKINETIC_HOST=${node_ip} com.seagate.kinetic.simulator.client.sanity.BasicAPISanityExample 1>${LOG_DIR}/${node_ip}.log 2>&1 & 
        exec "$JAVA" -classpath "$CLASSPATH" -Dkinetic.io.in=true -Dkinetic.io.out=true -DRUN_SSL_TEST=true -DKINETIC_HOST=${node_ip} com.seagate.kinetic.simulator.client.sanity.AdminAPISanityExample 1>${LOG_DIR}/${node_ip}.log 2>&1 &
        total_nodes=$(( total_nodes + 1 )) 
    fi
done

for((i=0;i<2*${total_nodes};i++)); 
do
	j=$(echo "$i+1" | bc -l) 
    wait %$j 
done

wait $!
exit 0
