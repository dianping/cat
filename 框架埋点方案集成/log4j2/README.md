## Log4j2配置

如果需要使用Cat自定义的Appender，需要在log4j2.xml中添加如下配置：

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="info">
    <Appenders>
        <Console name="console" target="SYSTEM_OUT">
            <ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY" />
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%-5p]  %c {%F:%L} - %m%n" />
        </Console>
        <CatAppender name="CatAppender"/>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="console" />
            <AppenderRef ref="CatAppender" />
        </Root>
    </Loggers>
</Configuration>
```
