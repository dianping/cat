#!/bin/bash
echo "initialize client.xml"
echo "<?xml version=\"1.0\" encoding=\"utf-8\"?><config mode=\"client\"><servers><server ip=\"${SERVER_URL}\" port=\"2280\" http-port=\"8080\"/></servers></config>" > /data/appdatas/cat/client.xml
#echo "$(sed 's/SERVER_URL/${SERVER_URL}/g' /data/appdatas/cat/client.xml)" > /data/appdatas/cat/client.xml
