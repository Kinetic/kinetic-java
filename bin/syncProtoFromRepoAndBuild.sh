#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PROTO_DIR=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/
PROTO_FILE=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
CLONE_DIR=$BASE_DIR/bin/Kinetic-ProtocoL
PROTO_COMPILE_DIR=$BASE_DIR/kinetic-common/src/main/java/

if [ -f "$PROTO_FILE" ]; then
    echo "$PROTO_FILE exists, does not sync and build, exit."
    exit 0
fi

if [ -d "$CLONE_DIR" ]; then
    rm -rf "$CLONE_DIR"
fi

if [ $# -eq 0 ]; then
    echo "Clone protocol file from github:"
    git clone https://github.com/Seagate/Kinetic-Protocol.git $CLONE_DIR
fi

if [ $# -eq 1 ]; then
    echo "Clone protocol file $1 from github:"
    git clone https://github.com/Seagate/Kinetic-Protocol.git $CLONE_DIR
    cd $CLONE_DIR
    git checkout $1
    echo "$1"
fi

cp $CLONE_DIR/kinetic.proto $PROTO_FILE

rm -rf "$CLONE_DIR"

echo "Sync protocol file finished."

echo "Compile protocol file: $PROTO_FILE."
protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_FILE

echo "Compile protocol file: $PROTO_DIR/kineticDb.proto."
protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticDb.proto

echo "Compile protocol file: $PROTO_DIR/kineticIo.proto."
protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticIo.proto

echo "Compile finished."

exit 0
