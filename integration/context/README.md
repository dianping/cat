## context调用链上下文通用类

本文件夹包含以下内容：

- CatConstantsExt.java 
```
1、继承、扩展CatConstants常量类，添加一些常用的Type
2、添加header常量，用于http协议传输rootId、parentId、childId三个context属性
```
- CatContextImpl.java
```
Cat.context接口实现类，用于context调用链传递，相关方法Cat.logRemoteCall()和Cat.logRemoteServer()
```

- CatContextServletFilter.java
```
http协议传输，远程调用链目标端接收context的filter，
通过header接收rootId、parentId、childId并放入CatContextImpl中，
调用Cat.logRemoteCallServer()进行调用链关联
使用方法（视项目框架而定）：
     1、web项目：在web.xml中引用此filter(请参考 https://github.com/dianping/cat/tree/master/integration/URL)
     2、Springboot项目，通过注入bean的方式注入此filter(请参考 https://github.com/dianping/cat/tree/master/integration/spring-boot)
注:若不涉及调用链，则直接使用cat-client.jar中提供的filter即可
```
- CatFeignConfiguration.java

```
 适用于feign调用其他SpringCloud微服务的调用链上下文传递场景
 作用：在使用feign请求其他微服务时，自动生成context上下文，并将相应参数rootId、parentId、childId放入header
 使用方法：在需要添加catcontext的feign service接口中，@FeignClient注解添加此类的configuration配置，
       如：@FeignClient(name="account-manage", configuration = CatFeignConfiguration.class)
```