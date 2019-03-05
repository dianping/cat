#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import cat


def test1():
    cat.metric("metric1").count()


def test2():
    cat.metric("metric1").count(2)


def test3():
    cat.metric("metric2").duration(152)


if __name__ == "__main__":
    cat.init("pycat")
    for i in range(1000):
        test1()
        test2()
        test3()

    import time
    time.sleep(1)
