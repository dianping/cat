#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import cat
import traceback


def test1():
    # 正常用法，默认是成功状态
    cat.log_event("Event", "E1")

    # 可以提供第三个参数，只要是非 0 的 string 类型都是失败
    cat.log_event("Event", "E2", cat.CAT_ERROR)
    cat.log_event("Event", "E3", "failed")

    # 可以提供第四个参数，增加 debug 信息
    cat.log_event("Event", "E4", "failed", "some debug info")


def test2():
    try:
        raise Exception("I'm a exception")
    except Exception as e:
        cat.log_exception(e)


def test3():
    try:
        1 / 0
    except Exception as e:
        cat.log_exception(e, traceback.format_exc())


def test4():
    cat.log_error("e1")


def test5():
    try:
        1 / 0
    except Exception:
        cat.log_error("e2", traceback.format_exc())


if __name__ == "__main__":
    cat.init("pycat")
    for i in range(1000):
        test1()
        test2()
        test3()
        test4()
        test5()

    import time
    time.sleep(1)
