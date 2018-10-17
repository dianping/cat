# 启动 cat 客户端前的准备工作

1. 创建 `/data/appdatas/cat` 目录

    确保你具有这个目录的读写权限。

2. 创建 `/data/applogs/cat` 目录 (可选)

    这个目录是用于存放运行时日志的，这将会对调试提供很大帮助，同样需要读写权限。

3. 创建 `/data/appdatas/cat/client.xml`，内容如下

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
        <servers>
            <server ip="<cat server ip address>" port="2280" http-port="8080" />
        </servers>
    </config>
    ```

    > 不要忘记把 **\<cat server IP address\>** 替换成你自己的服务器地址哦！
