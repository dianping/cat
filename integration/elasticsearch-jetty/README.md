# 兼容 elasticsearch 1.5.2
# 安装
```
$ git clone install git@github.com:ahuazhu/elasticsearch-jetty-cat.git
$ cd elasticsearch-jetty-cat
$ mvn clean install
$ cp target/releases/elasticsearch-jetty-cat-1.0.0-SNAPSHOT.zip /tmp/
$ cd ELASTIC_SEARCH_HOME
$ bin/plugin -url file:///tmp/elasticsearch-jetty-cat-1.0.0-SNAPSHOT.zip -install elasticsearch-jetty-cat
$ echo 'http.type: com.smzdm.elasticsearch.http.jetty.JettyHttpServerTransport' >> config/elasticsearch.yml
$ bin/elasticsearch restart
```
