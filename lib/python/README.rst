===============================
Cat-Python - Cat SDK for Python
===============================

Python SDK of `CAT <https://github.com/dianping/cat>`.

Installation
============

Install via pip:

.. code:: bash

    $ pip install cat-sdk

Code Demo
=========

.. code:: python

    import cat
    import time

    cat.init("appkey")

    with cat.Transaction("foo", "bar") as t:
        try:
            t.add_data("hello")
            t.add_data("foo", "bar")
            cat.log_event("hook", "before")
            # do something
        except Exception as e:
            cat.log_exception(e)
        finally:
            cat.metric("api-count").count()
            cat.metric("api-duration").duration(100)
            cat.log_event("hook", "after")
    time.sleep(1)

Documentation
=============

Please check `Github <https://github.com/dianping/cat/tree/master/lib/python>`_ for more information.

Changelog
=========

https://github.com/dianping/cat/blob/master/lib/python/CHANGELOG.md