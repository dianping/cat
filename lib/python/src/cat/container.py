#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import logging

log = logging.getLogger()


class idleCalled(object):
    def __init__(self, *args, **kwargs):
        pass

    def __getattr__(self, item):
        def run(*args, **kwargs):
            pass
        return run


class Container(object):

    __instances = {}

    def put(self, name, instance):
        self.__instances[name] = instance

    def get(self, name):
        return self.__instances.get(name)

    def contains(self, name):
        return name in self.__instances


container = Container()


def sdk():
    x = container.get("catsdk")
    if x is None:
        log.warning("cat sdk has not been initialized!")
        return idleCalled()
    return x
