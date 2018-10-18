## 最新特性一览 (版本：V3.0.0)

### 客户端

#### 1. 生态体系完善

CAT3.0推出多种语言的客户端，基本覆盖了主流开发语言。

目前支持：

* [**Java**](https://github.com/dianping/cat/blob/master/lib/java)
* [**C**](https://github.com/dianping/cat/blob/master/lib/c)
* [**C++**](https://github.com/dianping/cat/blob/master/lib/cpp)
* [**Python**](https://github.com/dianping/cat/blob/master/lib/python)
* [**Go**](https://github.com/dianping/cat/blob/master/lib/go)
* [**Node.js**](https://github.com/dianping/cat/blob/master/lib/node.js)

#### 2. 消息采样聚合
       
CAT3.0支持采样聚合，当采样命中或者内存队列已满时都会经过采样聚合上报。采样聚合是对消息树拆分归类，利用本地内存做分类统计，将聚合之后的数据进行上报，这样可以减少客户端的消息量以及降低网络开销。

#### 3. 精简依赖
       
CAT客户端3.0大幅精简了依赖，避免了依赖冲突。

### 服务端

#### 1. 自研二进制序列化协议

CAT 序列化协议自定义文本协议升级为自定义二进制协议，在大规模数据实时处理场景下性能提升显著。 

#### 2. 全新文件存储引擎

CAT3.0采用了全新文件存储引擎，大幅度降低了文件数量，减少了随机IO，显著提升IO性能。