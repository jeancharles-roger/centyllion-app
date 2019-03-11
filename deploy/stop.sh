#!/bin/sh

PIDFILE=server.pid

if [ -f "$PIDFILE" ]; then
    echo "Stopping server"
    kill -9 $(cat $PIDFILE)
    rm $PIDFILE
fi

