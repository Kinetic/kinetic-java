#! /usr/bin/env bash
BASE_DIR=`dirname "$0"`/..
BASE_DIR=`cd "$BASE_DIR"; pwd`
#echo "BASE_DIR=$BASE_DIR"

CLONE_DIR=$BASE_DIR/bin/Kinetic-ProtocoL

if [ -d "$CLONE_DIR" ]; then
    rm -rf "$CLONE_DIR"
fi

if [ $# -eq 0 ]; then
    echo "Clone protocol file from github:"
    git clone https://github.com/Seagate/kinetic-protocol.git $CLONE_DIR
fi

if [ $# -eq 1 ]; then
    echo "Clone protocol file $1 from github:"
    git clone https://github.com/Seagate/kinetic-protocol.git $CLONE_DIR
    cd $CLONE_DIR
    git checkout $1
    echo "$1"
fi

cp $CLONE_DIR/kinetic.proto $BASE_DIR/kinetic-common/src/main/java/com/seagate/kinetic/proto/kinetic.proto

rm -rf "$CLONE_DIR"

echo "Sync protocol file finished."

exit 0
