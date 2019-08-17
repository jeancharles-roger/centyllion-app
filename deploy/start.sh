#!/bin/sh

if [ ! -d "logs" ]; then
    mkdir logs
fi

PIDFILE=$PWD/server.pid
LOGFILE=$PWD/logs/server.log

DB_HOST=rj88488-001.dbaas.ovh.net
DB_PORT=35292

if [ -e $PWD/port ]
then
    ACTUAL_PORT=`cat $PWD/port`;
else if
    ACTUAL_PORT=${PORT:-9300};
fi

echo "Starting server from $PWD"

nohup \
    java -cp `find libs | xargs | sed "s/ /:/g"` com.centyllion.backend.MainKt \
        --debug --db-host $DB_HOST --db-port $DB_PORT \
        --keystore ../centyllion.jks --password $PASSWORD \
        --port $ACTUAL_PORT $* > $LOGFILE 2>&1 &

echo $! > $PIDFILE
