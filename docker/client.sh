#!/usr/bin/env bash
echo "$(sed 's/SERVER_URL/${SERVER_URL}/g' /data/appdatas/cat/client.xml)" > /data/appdatas/cat/client.xml
