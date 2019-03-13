# Cat Client for Python

`pycat` 同时支持 python2 (>=2.7) 和 python3 (>=3.5)。

但这也意味着 `centos6` 默认情况下是不被支持的（因为内置的 python 版本是 2.6.6）。

尽管如此，你仍可以通过升级内置 python 版本或使用 virtualenv 的方式使用 `pycat`。

## 安装

### 通过 pip 安装

```bash
pip install cat-sdk
```

### 通过 setuptools 安装

```bash
python setup.py install
```

## 初始化

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `pycat` 之前完成。

然后你就可以通过下面的代码初始化 `pycat` 了：

```python
cat.init("appkey")
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

### 协程模式

由于我们在 `ccat` 中使用 `ThreadLocal` 存储 Transaction 的栈，并用于构建消息树，同时 `pycat` 高度依赖 `ccat`。

因此在协程模式下，如 `gevent`, `greenlet`，由于同一个线程里的线程会交替执行，我们暂不提供消息树功能。

在这些情况下，你需要通过下述代码来关闭消息树功能。

```python
cat.init("appkey", logview=False)
```

这样我们就会禁用 ccat 的上下文管理器，从而禁用消息树功能。

### 采样聚合

采样聚合在默认的情况下是开启的

```python
cat.init("appkey", sampling=False)
```

### 编码器

默认的编码器是**二进制**，你可以切换到**文本**，以适配早期版本的 cat 服务端。

```python
cat.init("appkey", encoder=cat.ENCODER_TEXT)
```

### 调试日志

有时你会想要打开调试日志。

注意调试日志会被输出到控制台中。

```python
cat.init("appkey", debug=True)
```

## Quickstart

```python
import cat
import time

cat.init("appkey")

with cat.Transaction("foo", "bar") as t:
    try:
        t.add_data("a=1")
        cat.log_event("hook", "before")
        # do something
    except Exception as e:
        cat.log_exception(e)
    finally:
        cat.metric("api-count").count()
        cat.metric("api-duration").duration(100)
        cat.log_event("hook", "after")

time.sleep(1)
```

## API List

### Transaction

```python
t = cat.Transaction("Trans", "t3")
t.complete()
```

为了避免忘记关闭 Transaction，我们强烈建议使用 try-finally 代码块包裹 transaction，并在 finally 代码块中执行 complete。

```python
try:
	t = cat.Transaction("Trans", "t3")
finally:
	t.complete()
```

我们同时提供了`装饰器`和`上下文管理器`的用法，可以自动关闭 Transaction。

这也是我们推荐的使用方法。

#### via decorator

```python
@cat.transaction("Trans", "T2")
def test():
    '''
    Use with decorator
    '''
    cat.log_event("Event", "E2")
```

如果被装饰的函数出现什么问题，Transaction 的状态会被置为 `FAILED`，并且无论有没有 Exception 被抛出，Transaction 都会被自动关闭。

唯一的问题就是如果使用装饰器模式的话，你拿不到 Transaction 对象。

#### via context manager

```python
with cat.Transaction("Transaction", "T1") as t:
    cat.log_event("Event", "E1")
    try:
        do_something()
    except Exception:
        t.set_status(cat.CAT_ERROR)
    t.add_data("hello world!")
```

如果在 `with` 管理的上下文中出现了什么问题，Transaction 的状态会被置为 `FAILED`，并且无论有没有 Exception 被抛出，Transaction 都会被自动关闭。

虽然这有些复杂，但你可以拿到 transaction 对象。

### Transaction apis

我们提供了一系列 API 来对 Transaction 进行修改。

* add\_data
* set\_status
* set\_duration
* set\_duration\_start
* set\_timestamp
* complete

这些 API 可以被很方便的使用，如下代码所示：

```python
try:
    trans = cat.Transaction("Trans", "T3")
    trans.add_data("content")
    trans.add_data("key", "val")
    trans.set_status("error")
    trans.set_duration(500)
    trans.set_duration_start(time.time() * 1000 - 30 * 1000)
    trans.set_timestamp(time.time() * 1000 - 30 * 1000)
finally:
    # NOTE don't forget to complete the Transaction!
    trans.complete()
```

在使用 Transaction 提供的 API 时，你可能需要注意以下几点：

1. 你可以调用 `add_data` 多次，他们会被 `&` 连接起来。
2. 同时指定 `duration` 和 `durationStart` 是没有意义的，尽管我们在样例中这样做了。
3. 不要忘记完成 transaction！否则你会得到一个毁坏的消息树以及内存泄漏！

### Event

#### cat.log_event

```python
# Log a event with success status and empty data.
cat.log_event("Event", "E1")

# The 3rd parameter (status) is optional, default is "0".
# It can be any of string value.
# The event will be treated as a "problem" unless the given status == cat.CAT_CUSSESS ("0")
# which will be recorded in our problem report.
cat.log_event("Event", "E2", cat.CAT_ERROR)
cat.log_event("Event", "E3", "failed")

# The 4th parameter (data) is optional, default is "".
# It can be any of string value.
cat.log_event("Event", "E4", "failed", "some debug info")
```

#### cat.log_exception

记录一个 Exception

Exception 是一种特殊的 Event，默认情况下，`type = Exception`，`name = exc.__class__.__name__`

由于 Exception 通常出现在 except 代码块中，错误堆栈信息也会被自动收集和上报。

```python
try:
    raise Exception("I'm a exception")
except Exception as e:
    cat.log_exception(e)

# We will collect error traces automatically in most cases
# But you can also customize the trace info.
try:
    1 / 0
except Exception as e:
    cat.log_exception(e, traceback.format_exc())

# Even out of an except block.
e = Exception("something goes wrong")
cat.log_exception(e, "customized trace info")
```

#### cat.log_error

记录一个 Error

Error 是一个轻量级的 Exception，默认情况下，`type = Exception`，`name` 通过第一个参数指定。

```python
# Same as cat.log_event("Exception", "e1")
cat.log_error("e1")

# Error traces will be collected when you use it in an except block.
try:
    1 / 0
except Exception:
    cat.log_error("e2")

# customize your own error traces through the 2nd parameter which is optional.
cat.log_error("e3", "this is my error stack info")
```

### Metric

```python
# Counter
cat.metric("metric1").count() # default is 1
cat.metric("metric1").count(5)

# Duration
cat.metric("metric2").duration(100)
```

我们每秒会聚合 metric。

举例来说，如果你在同一秒调用 count 三次（相同的 name），我们会累加他们的值，并且一次性上报给服务端。

在 `duration` 的情况下，我们用平均值来取代累加值。
