# DUBBO和cat整合插件

主要目的是能够监控当前系统dubbo调用执行情况，比如耗时以及异常统计

## Getting start

 引入插件包

```xml
 <dependency>
       <groupId>net.dubboclub</groupId>
        <artifactId>cat-monitor</artifactId>
         <version>0.0.6</version>
     </dependency>
```

# That's All!

项目添加以上依赖将会在cat里面出现cross报表，dependency，服务端的matrix以及调用链路的trace信息（1.0.8以后（包含1.0.8））

## 手动开启/关闭cat监控
```java
DubboCat.disable();
//开启
DubboCat.enable();
```

