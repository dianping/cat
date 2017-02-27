## Logback配置

如果需要使用Cat自定义的Appender，需要在logback.xml中添加如下配置：

```
    <appender name="CatAppender" class="com.dianping.cat.logback.CatLogbackAppender"></appender>

    <root level="info">
        <appender-ref ref="CatAppender" />
    </root>
```