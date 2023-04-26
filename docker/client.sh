#!/usr/bin/env bash
echo "initialize client.xml"
echo "$(sed 's/SERVER_URL/${SERVER_URL}/g' /data/appdatas/cat/client.xml)" > /data/appdatas/cat/client.xml
