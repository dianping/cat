#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import cat
import random
import json
import time


def func(method, args):
    r = random.random()
    # 5% of the requests will raise exception
    if r < 0.95:
        time.sleep(random.random() / 100)
    else:
        raise Exception("something wrong!")


@cat.transaction("rpc", "server")
def serve(method, args):
    cat.log_event("hook", "receive")
    with cat.Transaction("serve", method) as t:
        try:
            t.add_data(json.dumps(args))
            t.add_data("foo", "bar")
            cat.log_event("hook", "before1")
            cat.log_event("hook", "before2")
            cat.log_event("hook", "before3")
            res = func(method, args)
        finally:
            cat.metric("rpc-count").count()
            cat.metric("rpc-duration").duration(100)
            cat.log_event("hook", "after")
    return res


if __name__ == '__main__':
    cat.init("pycat", logview=True)
    while True:
        try:
            serve('ping', ["a", {"b": 1}])
        except Exception:
            pass
        time.sleep(0.1)
    time.sleep(1)
