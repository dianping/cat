#!/usr/bin/env bash
echo "initialize client.xml"

IFS=',' read -ra URL_ARRAY <<< "${SERVER_URLS}"

sed -i '/<server ip="SERVER_URL" port="2280" http-port="8080"\/>/d' /data/appdatas/cat/client.xml

for url in "${URL_ARRAY[@]}"; do
    sed -i "/<servers>/a <server ip=\"${url}\" port=\"2280\" http-port=\"8080\"\/>" /data/appdatas/cat/client.xml
done
