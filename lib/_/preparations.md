# Preparations before initializing cat client.

1. Create `/data/appdatas/cat` directory.

    Make sure that you have **read and write (>=0644)** permission of the created directory.

2. Create `/data/applogs/cat` directory. (optional)

    This directory is used for preserving debug logs, which can be very useful while debugging, **read and write** permission is also required.

3. Create `/data/appdatas/cat/client.xml` with the following contents.

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
        <servers>
            <server ip="<cat server ip address>" port="2280" http-port="8080" />
        </servers>
    </config>
    ```

    > Don't forget to change the **\<cat server IP address\>** to your server ip address.
