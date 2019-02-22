#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@baixing.com>

# Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import functools
import traceback

from .const import CAT_ERROR
from .container import sdk
from .event import log_exception
from .message import NullMessage
from .sdk import PyTransaction
from .version import _

__all__ = ['Transaction', 'transaction']


class Transaction(NullMessage):

    def __init__(self, mtype, mname):
        self._trans = sdk().new_transaction(mtype, mname)
        self._py = isinstance(self._trans, PyTransaction)
        self._completed = False

    def complete(self):
        if not self._completed:
            self._trans.complete(self._trans)
            self._completed = True
        return self

    def set_status(self, status):
        if self._py:
            self._trans.setStatus(self._trans, status)
        else:
            self._trans.setStatus(self._trans, _(status))
        return self

    def __add_data(self, data):
        if self._py:
            self._trans.addData(self._trans, data)
        else:
            self._trans.addData(self._trans, _(data))

    def __add_kv(self, key, val):
        if self._py:
            self._trans.addKV(self._trans, key, val)
        else:
            self._trans.addKV(self._trans, _(key), _(val))

    def add_data(self, data, val=None):
        if val is None:
            self.__add_data(data)
        else:
            self.__add_kv(data, val)
        return self

    def set_duration(self, duration):
        self._trans.setDurationInMillis(self._trans, int(duration))
        return self

    def set_duration_start(self, timestamp):
        self._trans.setDurationStart(self._trans, int(timestamp))
        return self

    def set_timestamp(self, timestamp):
        self._trans.setTimestamp(self._trans, int(timestamp))
        return self

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is not None:
            trace = traceback.format_exception(exc_type, exc_val, exc_tb)
            log_exception(
                exc_val,
                "\n".join(trace),
            )
            self.set_status(CAT_ERROR)
        self.complete()


def transaction(mtype, mname):
    def wrapper(func):
        @functools.wraps(func)
        def wraps(*args, **kwargs):
            with Transaction(mtype, mname):
                return func(*args, **kwargs)
        return wraps
    return wrapper
