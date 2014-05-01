#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

echo "Compile protocol file."
protoc --proto_path=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto --java_out=$BASE_DIR/kinetic-common/src/main/java/ $BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
protoc --proto_path=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto --java_out=$BASE_DIR/kinetic-common/src/main/java/ $BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kineticDb.proto
protoc --proto_path=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto --java_out=$BASE_DIR/kinetic-common/src/main/java/ $BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kineticIo.proto

echo "Compile finished."

exit 0