## 最新特性一览 (版本：3.0.0)

**重大更新，国庆献礼：Cat进入3.0时代！** 更强劲的性能、更完善的体系，快来使用Cat吧:D

### 客户端

#### 1. 生态体系完善

Cat3.0推出多种语言的客户端，基本覆盖了主流开发语言，一定能够满足您的需求！

目前支持：

* [**Java**](https://github.com/dianping/cat/blob/master/lib/java)
* [**C**](https://github.com/dianping/cat/blob/master/lib/c)
* [**C++**](https://github.com/dianping/cat/blob/master/lib/cpp)
* [**Python**](https://github.com/dianping/cat/blob/master/lib/python)
* [**Go**](https://github.com/dianping/cat/blob/master/lib/go)
* [**Node.js**](https://github.com/dianping/cat/blob/master/lib/node.js)

#### 2. 消息采样聚合
        
Cat3.0支持采样聚合，不影响宏观指标的前提下，降低了消息量与网络开销，使得Cat更健壮！

#### 3. 精简依赖
        
Cat客户端3.0大幅精简了依赖，避免了依赖冲突，使用Cat更便捷！


### 服务端

#### 1. 自研二进制序列化协议

Cat3.0使用二进制协议序列化传输对象，代替老版本以文本协议序列化的方式，大幅提升序列化性能，使得Cat更高效！ 

#### 2. 全新文件存储引擎

Cat3.0采用了全新文件存储引擎，大幅度降低了文件数量，减少了随机IO，显著提升IO性能，使得Cat更强大！