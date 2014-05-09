#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PROTO_REPO_URL=https://github.com/Seagate/Kinetic-Protocol.git
PROTO_DIR=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/
PROTO_FILE=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
CLONE_DIR=$BASE_DIR/bin/Kinetic-ProtocoL
PROTO_COMPILE_DIR=$BASE_DIR/kinetic-common/src/main/java/
PROTO_COMMIT_HASH=9c6b4a180a70f8488c5d8ec8e8a6464c4ff63f84

function syncFromProtoRepo(){
    if [ -d "$CLONE_DIR" ]; then
        rm -rf "$CLONE_DIR"
    fi

    if [ $# -eq 0 ]; then
        echo "Clone protocol file from github:"
        git clone $PROTO_REPO_URL $CLONE_DIR
        cd $CLONE_DIR
        git checkout $PROTO_COMMIT_HASH
    fi

    cp $CLONE_DIR/kinetic.proto $PROTO_FILE

    rm -rf "$CLONE_DIR"

    echo "Sync protocol file finished."
}

function compileProto(){
    echo "Compile protocol file: $PROTO_FILE."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_FILE

    echo "Compile protocol file: $PROTO_DIR/kineticDb.proto."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticDb.proto

    echo "Compile protocol file: $PROTO_DIR/kineticIo.proto."
    protoc --proto_path=$PROTO_DIR --java_out=$PROTO_COMPILE_DIR $PROTO_DIR/kineticIo.proto

    echo "Compile finished."
}

if [ -f "$PROTO_FILE" ]; then
    echo "$PROTO_FILE exists, does not sync from repo."
else
    echo "$PROTO_FILE does not exists, sync from repo"
    syncFromProtoRepo
fi

compileProto

exit 0