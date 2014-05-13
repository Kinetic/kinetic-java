#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PROTO_REPO_URL=https://github.com/Seagate/Kinetic-Protocol.git
PROTO_FILE=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
CLONE_DIR=$BASE_DIR/bin/Kinetic-ProtocoL

function syncFromProtoRepo(){
    if [ -d "$CLONE_DIR" ]; then
        rm -rf "$CLONE_DIR"
    fi

    if [ $# -eq 0 ]; then
        echo "Clone the latest protocol file from github:"
        git clone $PROTO_REPO_URL $CLONE_DIR
    fi

    cp $CLONE_DIR/kinetic.proto $PROTO_FILE

    rm -rf "$CLONE_DIR"

    echo "Sync protocol file finished."
}

syncFromProtoRepo

exit 0