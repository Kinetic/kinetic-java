#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

PROTO_REPO_URL=https://github.com/Kinetic/Kinetic-Protocol.git
PROTO_FILE=$BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto
CLONE_DIR=$BASE_DIR/bin/Kinetic-Protocol
PROTO_RELEASE_VERSION=3.0.5

function syncFromProtoRepo(){
    if [ -d "$CLONE_DIR" ]; then
        rm -rf "$CLONE_DIR"
    fi

    if [ $# -eq 0 ]; then
        echo "Clone protocol file from github, the proto release version is: $PROTO_RELEASE_VERSION"
        git clone $PROTO_REPO_URL $CLONE_DIR
        cd $CLONE_DIR
        git checkout $PROTO_RELEASE_VERSION
    fi

    cp $CLONE_DIR/kinetic.proto $PROTO_FILE

    rm -rf "$CLONE_DIR"

    echo "Sync protocol file finished."
}

syncFromProtoRepo

exit 0
