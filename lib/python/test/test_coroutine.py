#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@baixing.com>

import cat
import time


def test2():
    '''
    Use with context manager
    '''
    with cat.Transaction("Transaction", "test2") as trans:
        trans.set_status(cat.CAT_ERROR)
        cat.log_event("h", "v")
        trans.add_data("a=1")
        trans.add_data("b=2")


if __name__ == '__main__':
    cat.switch(cat.MODE_COROUTINE)
    cat.init("pycat")
    for i in range(1000):
        print(i)
        test2()
    time.sleep(1)
