## 最新特性一览 (版本：3.0.0)

### 1. 多语言客户端

Cat推出多种语言的客户端，基本覆盖了主流开发语言，一定能够满足您的需求！

Cat目前支持：

* [**Java**](https://github.com/dianping/cat/blob/master/lib/java)
* [**C**](https://github.com/dianping/cat/blob/master/lib/c)
* [**C++**](https://github.com/dianping/cat/blob/master/lib/cpp)
* [**Python**](https://github.com/dianping/cat/blob/master/lib/python)
* [**Go**](https://github.com/dianping/cat/blob/master/lib/go)
* [**Node.js**](https://github.com/dianping/cat/blob/master/lib/node.js)


### 2. 自研二进制序列化协议

新版本Cat使用二进制协议序列化传输对象，代替老版本以文本协议序列化的方式，大幅提升序列化性能，使得Cat更高效！


### 3. 消息采样聚合

新版本Cat支持采样聚合，不影响宏观指标（总量个数、平均延时、分位线等）的前提下，降低了消息量与网络开销，使得Cat更健壮！


### 4. 新版消息文件存储

新版本Cat采用二级索引存储方案，减少了随机IO，大幅提升IO性能，使得Cat更强大！