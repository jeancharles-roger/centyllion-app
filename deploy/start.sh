#!/bin/sh

mkdir logs

PIDFILE=$PWD/server.pid
LOGFILE=$PWD/logs/server.log

DB_HOST=rj88488-001.dbaas.ovh.net
DB_PORT=35292

if [ -e $PWD/port ]
then
    PORT=`cat $PWD/port`;
else
    PORT=9300;
fi

echo "Starting server from $PWD"

nohup \
    java -cp `find libs | xargs | sed "s/ /:/g"` com.centyllion.backend.MainKt --db-host $DB_HOST --db-port $DB_PORT --db-password $DB_PASSWORD --port $PORT $* > $LOGFILE 2>&1 &

echo $! > $PIDFILE
