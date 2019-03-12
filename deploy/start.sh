#!/bin/sh

mkdir logs

PIDFILE=$PWD/server.pid
LOGFILE=$PWD/logs/server.log

if [ -e $PWD/port ]
then
    PORT=`cat $PWD/port`;
else
    PORT=9300;
fi

echo "Starting server from $PWD"

nohup \
    java -cp `find libs | xargs | sed "s/ /:/g"` com.centyllion.backend.MainKt $DEBUG --port $PORT > $LOGFILE 2>&1 &

echo $! > $PIDFILE
