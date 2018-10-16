## CAT报表

CAT提供以下几种报表：

- **Transaction实时报表**    一段代码运行时间、次数，比如URL、Cache、SQL执行次数和响应时间 

- **Event实时报表**    一行代码运行次数，比如出现一个异常 

- **Problem实时报表**    根据Transaction/Event数据分析出来系统可能出现的异常，包括访问较慢的程序等 

- **Heartbeat实时报表**    JVM内部一些状态信息，比如Memory，Thread等

- **Business实时报表**    业务监控报表，比如订单指标。与Transaction、Event、Problem不同，Business更偏向于宏观上的指标，另外三者偏向于微观代码的执行情况