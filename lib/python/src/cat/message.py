#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

from .const import (
    CAT_SUCCESS,
    CAT_ERROR
)


class NullMessage(object):

    CAT_SUCCESS = CAT_SUCCESS
    CAT_ERROR = CAT_ERROR

    def __init__(self, mtype, mname):
        pass

    def complete(self):
        pass

    def set_status(self, status):
        pass

    def set_data(self, data):
        pass
