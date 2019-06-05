#!/bin/sh

sleep 2

exec bin/inandout -p 8443 -d data/ -c data/webserver.id -s ServersPasswordKey.txt
