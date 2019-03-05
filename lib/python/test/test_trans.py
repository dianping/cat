#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@baixing.com>

import argparse
import cat
import time


def ignore_exception(func):
    def wraps(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception:
            pass
    return wraps


@ignore_exception
@cat.transaction("Trans", "T1")
def test1():
    '''
    Use via decorator
    '''
    print(1 / 0)  # NOTE will cause ZeroDivisionException


def test2():
    '''
    Use via context manager
    '''

    def do_something():
        import random
        if random.random() < 0.1:
            raise Exception("error occured!")

    with cat.Transaction("Trans", "T2") as t:
        cat.log_event("Event", "E2")
        try:
            do_something()
        except Exception:
            t.set_status(cat.CAT_ERROR)
        t.add_data("context-manager")
        t.add_data("foo", "bar")


def test3():
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


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--logview', action='store_true', default=False)
    args = parser.parse_args()

    cat.init("pycat", debug=True, logview=args.logview)

    for i in range(100):
        test1()
        test2()
        test3()
        time.sleep(0.01)
    time.sleep(1)
