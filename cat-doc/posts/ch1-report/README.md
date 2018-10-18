## CAT报表

CAT提供以下几种报表：

- **[Transaction报表](transaction.md)**    一段代码运行时间、次数，比如URL、Cache、SQL执行次数和响应时间 

- **[Event报表](event.md)**    一行代码运行次数，比如出现一个异常 

- **[Problem报表](problem.md)**    根据Transaction/Event数据分析出来系统可能出现的异常，包括访问较慢的程序等 

- **[Heartbeat报表](heartbeat.md)**    JVM内部一些状态信息，比如Memory，Thread等

- **[Business报表](business.md)**    业务监控报表，比如订单指标。与Transaction、Event、Problem不同，Business更偏向于宏观上的指标，另外三者偏向于微观代码的执行情况