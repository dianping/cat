# Cat Client Python

[中文文档](./doc/zh-CN.md)

The pycat can be used both in python2 (>=2.7) and python3 (>=3.6)

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

First of all, you have to create `/data/appdatas/cat` directory, read and write permission is required (0644).`/data/applogs/cat` is also required if you'd like to preserve a debug log, it can be very useful while debugging.

And create a config file `/data/appdatas/cat/client.xml` with the following contents.

```xml
<?xml version="1.0" encoding="utf-8"?>
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
    <servers>
        <server ip="<cat server ip address>" port="2280" http-port="8080" />
    </servers>
</config>
```

Don't forget to change the `<cat server IP address>` to your own after you copy and paste the contents.

With all the preparations have done, It's easy to initialize it in your python codes.

```python
cat.init("appkey")
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) is allowed in appkey.

# Coroutine Mode

As we using `ThreadLocal` to storage the Transaction stack in `ccat`, which is neccessary to build message tree, and `pycat` is highly depend on `ccat`. (through cffi)

So we don't support message tree in `coroutine` modes, like `gevent`, `greenlet`.

In these cases, you should use the following code to disable message tree.

```python
cat.init("appkey", message_tree=False)
```

And we will disable the context manager which is used for generating `message tree`.

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

## Api List

### Transaction

```python
t = cat.Transaction("Trans", "t3")
t.complete()
```

To avoid of forgetting to complete the Transaction, it's better to surround the Transaction by a try-finally block.

```python
try:
	t = cat.Transaction("Trans", "t3")
finally:
	t.complete()
```

We also provide `decorator` and `context manager` usages, which can complete the Transaction automatically.

And we highly recommend to use the Transaction in these ways.

#### via decorator

```python
@cat.transaction("Trans", "T2")
def test():
    '''
    Use with decorator
    '''
    cat.log_event("Event", "E2")
```

If something goes wrong in the decorated function, the status of the Transaction will be set to `FAILED`, and whatever the function raised a exception or not, the Transaction will be auto-completed.

The only problem is you can't get the Transaction object while you are using a decorator.

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

If something goes wrong in the `with` context, the status of the Transaction will be set to `FAILED`, and whatever the code block raised a exception or not, the Transaction will be auto-completed.

### Transaction apis

we offered a list of APIs to modify the Transaction.

* add\_data
* set\_status
* set\_duration
* set\_duration\_start
* set\_timestamp
* complete

These APIs can be easily used like the following codes.

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

There are something you have to know about the Transaction APIs:

1. `Duration` will be calculated while you complete the Transaction. (current\_timestamp - duration\_start)
1. Although `duration_start` is as same as `timestamp` by default, you can overwrite it.
2. `duration_start` and `timestamp` are different, the first one represents the start time of the Transaction, and the second one only means created time of a message. (Transaction is just a kind of message)
1. `duration_start` won't work when you specified the `duration`.
2. You can call `add_data` several times, the added data will be connected by `&`.

### Event

#### cat.log\_event

```python
# Log a event with success status and empty data.
cat.log_event("Event", "E1")

# The third parameter (status) is optional, default by "0".
# It can be any of string value.
# The event will be treated as "problem" unless the given status == cat.CAT_CUSSESS ("0")
# which will be recorded in our problem report.
cat.log_event("Event", "E2", cat.CAT_ERROR)
cat.log_event("Event", "E3", "failed")

# The fourth parameter (data) is optional, default by "".
# It can be any of string value.
cat.log_event("Event", "E4", "failed", "some debug info")
```

#### cat.log\_exception

Log an exception.

Exception is a special event, with `type = Exception` and `name = exc.__class__.__name__` by default.

Due to an exception is usually in an except block, the error traces will be automatically collected and reported.

```python
try:
    rase Exception("I'm a exception")
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

An error is a light exception, with `type = Exception` and name is given by the 1st parameter.

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

We do aggregate every seconds.

For example, if you called count 3 times in one second (with the same name), we will just summarised the value of them and reported once to the server.

In case of `duration`, we use `averaged` value instead of `summarised` value.
