## LOG集成
- cat支持Log4j、Log4j2、Logback的集成。
- cat仅仅关心<font color=#FF4500>log中有exception的日志，并且是error级别的log</font>，也就是error级别的异常监控。其他info，warn级别都不关心，error级别中，如果没有异常堆栈，cat也不关心。
- 在一个messageTree内部，cat内部会针对同样的异常，如果连续调用两次logError(e)，仅仅会上报第一份exception。注意框架打印了一个Exception，如果业务包装此异常为new BizException(e)，这样仍旧算两次异常，上报两份。


## Log4j配置

- 业务程序的所有异常都通过记录到CAT中，方便看到业务程序的问题，建议在Root节点中添加此Appendar

- 在Log4j的xml中，加入Cat的Appender
    
```
<appender name="catAppender" class="com.dianping.cat.log4j.CatAppender"></appender>

```

- 在Root的节点中加入catAppender

```
<root>
    <level value="error" />
    <appender-ref ref="catAppender" />
</root>
```
- 注意有一些Log的是不继承root的，需要如下配置

```
<logger name="com.dianping.api.location" additivity="false">
    <level value="INFO"/>
    <appender-ref ref="locationAppender"/>
    <appender-ref ref="catAppender"/>
</logger>
```