# erlang版的cat客户端

此erlang客户端实现几乎所有C客户端的API，如有本文档未涉及的方法请参考C端的说明。

## 安装

### 添加依赖

```config
{erl_opts, [debug_info]}.
{deps, [
    // 添加erlcat依赖
    {erlcat,{git,"https://github.com/glasses1989/erlcat.git","master"}}
]}.

{shell, [
  % {config, [{config, "config/sys.config"}]},
    {apps, [demo]}
]}.

```
### 编译

```sbtshell
$ rebar3 compile
```

## 初始化

然后你就可以通过下面的代码初始化erlang版的cat客户端了：

```erlang
%% 采用默认配置
erlcat:init_cat("appkey", #cat_config{})
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

默认采用如下配置：

* `encoderType` = 1.
* `enableHeartbeat` = 1.
* `enableSampling` = 1.
* `enableMultiprocessing` = 0.
* `enableDebugLog` = 0.

### 采样聚合

采样聚合在默认的情况下是开启的。

```erlang
erlcat:init_cat("appkey", #cat_config{enable_sampling=1})
```

### 编码器

默认的编码器是**二进制**，你可以切换到**文本**，以适配早期版本的cat服务端。

高版本的cat服务端请采用默认的**二进制**，**文本**方式会造成数据传输失败。

```erlang
erlcat:init_cat("appkey", #cat_config{encoder_type=1})
```

### 协程模式

由于我们在`ccat`中使用`ThreadLocal`存储 Transaction 的栈，并用于构建消息树。

默认是禁用ccat的上下文管理器，即禁用消息树功能。

```erlang
erlcat:init_cat("appkey", #cat_config{enable_multiprocessing=0})
```

### 调试日志

有时你会想要打开调试日志。

注意调试日志会被输出到控制台中。

```erlang
erlcat:init_cat("appkey", #cat_config{enable_debugLog=1})
```

## 文档

### 初始化上下文

每个erlang进程都需要初始化上下文对象。

解决多个进程共享上下文造成的事务等数据"混乱"的问题。

```erlang
%% 创建上下文对象，放入进程字典
ErlCatContext = erlcat:new_context(),
put(erlcat_process_context, ErlCatContext).
```

### Event

记录一个事件。

```erlang
%% 记录一个完整的事件
erlcat:log_event(ErlCatContext, "Event", "E4", "0", "some debug info");
```

记录一个带堆栈信息的错误，错误是一种特殊的event，默认情况下type=Exception且name=error。

name可以通过第二个参数来复写，错误堆栈会被收集并存放在 data 属性中。

```erlang
%% 记录一个错误事件
erlcat:log_error(ErlCatContext, "failed", "error info");
```

### Metric

metric每秒钟聚合指定指标（name）的数据。

举例来说，如果你在同一秒种调用了三次（使用相同的name），我们会对这些值求和并且一次性上报给服务端。

对于 `duration`，我们使用平均值来代替求和。

```erlang
%% 每秒求指标总和
erlcat:log_metric_for_count(ErlCatContext, "metric-1", 3),
%% 每秒求指标平均值
erlcat:log_metric_for_duration(ErlCatContext, "metric-2", 3),
%% 每秒求指标总和，和log_metric_for_count方法效果一样
erlcat:log_metric_for_sum(ErlCatContext, "metric-3", 3),
```
### Transaction apis

我们提供了一系列API来对Transaction进行创建和修改。

* new\_transaction
* add\_data
* add\_kv
* set\_status
* set\_duration
* set\_duration\_start
* set\_timestamp
* complete

这些 API 可以被很方便的使用，如下代码所示：

```erlang
    Tran1 = erlcat:new_transaction(ErlCatContext, "MSG.send", "send"),
    erlcat:add_kv(ErlCatContext, Tran1, "key", "val"),
    erlcat:set_status(ErlCatContext, Tran1, "error"),
    erlcat:set_duration(ErlCatContext, Tran1, 500),
    erlcat:set_duration_start(ErlCatContext, Tran1, time.time() * 1000 - 30 * 1000),
    erlcat:set_timestamp(ErlCatContext, Tran1, time.time() * 1000 - 30 * 1000),
    erlcat:complete(ErlCatContext, Tran1),
```

在使用Transaction提供的API时，你可能需要注意以下几点：

1. 你可以调用`add_data`多次，他们会被`&`连接起来；
2. 同时指定`duration`和`durationStart`是没有意义的，尽管我们在样例中这样做了；
3. 不要忘记完成transaction，否则你会得到一个毁坏的消息树以及内存泄漏；
