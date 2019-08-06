+ DockerHub地址: [rolesle/cat:0.0.1](https://hub.docker.com/r/rolesle/cat)
+ 改动的地方
    1. Dockerfile
         + tomcat和maven下载的地址和版本由国外改成国内
         + 版本改变: [7.0.79;5.2] -> [7.0.96;5.4]
    1. 将SERVER_IP指定加入catalina.sh中，在docker-compose.yaml中增加变量SERVER_IP为服务器的IP(保证跟其他机器可以互通)，问题详情: [#1763](https://github.com/dianping/cat/issues/1763)
    1. 修复tomcat和镜像时区不是东八区的问题
