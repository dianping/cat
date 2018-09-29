# Cat Client for Python

[中文文档](./README.zh-CN.md)

The `pycat` can be used both in python2 (>=2.6) and python3 (>=3.5)

## Installation

### via pip

```bash
pip install pycat
```

### via setuptools

```bash
python setup.py install
```

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `pycat`.

And then you can initialize `pycat` with the following codes:

```python
cat.init("appkey")
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) is allowed in appkey.

### Coroutine Mode

Since we are using `ThreadLocal` to storage the transaction stack in `ccat`, which is necessary to build the `message tree`, and `pycat` is highly dependent on `ccat`. (with cffi)

So we don't support message tree in `coroutine` modes, like `gevent`, `greenlet` because of different coroutines that in the same thread run alternately.

In these cases, you should use the following code to initialize `pycat`.

```python
cat.init("appkey", logview=False)
```

Then we will disable the context manager which is used for building `message tree`.

### Sampling

Sampling is enabled by default, you can disable it through the following codes.

```python
cat.init("appkey", sampling=False)
```

### Encoder

The default encoder is `binary encoder`, you can switch it to `text encoder`, which can be recognized by the earlier version of the cat server.

```python
cat.init("appkey", encoder=cat.ENCODER_TEXT)
```

### Debug log

Sometimes you may want to enable the debug log.

Note the logs will be outputted to `console`.

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

To avoid forgetting to complete the transaction, we strongly recommend you to surround the transaction by a `try-finally` block and complete the transaction in the `finally` code block.

```python
try:
    t = cat.Transaction("Trans", "t3")
finally:
    t.complete()
```

We also provide `decorator` and `context manager` usages, which can complete the transaction automatically.

And we highly recommend you to use the transaction in these ways.

#### via decorator

```python
@cat.transaction("Trans", "T2")
def test():
    '''
    Use with decorator
    '''
    cat.log_event("Event", "E2")
```

If something goes wrong in the decorated function, the status of the transaction will be set to `FAILED`, and whatever the function raised an exception or not, the transaction will be auto-completed.

The only problem is that you can't get the transaction object if you monitor a function via a decorator.

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

If something goes wrong in the `with` context, the status of the transaction will be set to `FAILED`, and whatever the code block raised an exception or not, the transaction will be auto-completed.

Though it is a bit complex, you can get the transaction object :)

### Transaction apis

We offered a series of APIs to modify the transaction.

* add\_data
* set\_status
* set\_duration
* set\_duration\_start
* set\_timestamp
* complete

These APIs can be easily used with the following codes.

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
    # NOTE don't forget to complete the transaction!
    trans.complete()
```

There is something you may want to know:

1. You can call `add_data` several times, the added data will be connected by `&`.
2. It's meaningless to specify `duration` and `durationStart` in the same transaction, although we do it in the example :)
3. Never forget to complete the transaction! Or you will get corrupted message trees and memory leaks!

### Event

#### cat.log\_event

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

#### cat.log\_exception

Log an exception.

Exception is a special event, with `type = Exception` and `name = exc.__class__.__name__` by default.

Due to an exception is usually in an except block, the error traces will be automatically collected and report.

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

#### cat.log\_error

Log an error.

Error is a light exception, with `type = Exception` and name is given by the 1st parameter.

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

We do aggregate every second.

For example, if you have called count 3 times in one second (with the same name), we will just summarise the value of them and report once to the server.

In the case of `duration`, we use `averaged` value instead of `summarised` value.
