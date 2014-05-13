#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PROTO_DIR=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/
PROTO_FILE=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
PROTO_COMPILE_DIR=$BASE_DIR/kinetic-common/src/main/java/

function compileProto(){
    echo "Compile protocol file: $PROTO_FILE."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_FILE

    echo "Compile protocol file: $PROTO_DIR/kineticDb.proto."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticDb.proto

    echo "Compile protocol file: $PROTO_DIR/kineticIo.proto."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticIo.proto

    echo "Compile finished."
}

compileProto

exit 0