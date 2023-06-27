#!/usr/bin/env bash
echo "initialize client.xml"
sed -i "s/SERVER_URL/${SERVER_URL}/g" /data/appdatas/cat/client.xml;
