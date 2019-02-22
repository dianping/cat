#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

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

import logging
import os
import platform
import time

from .catffi import ffi
from .const import (
    CAT_SUCCESS,
    ENCODER_BINARY,
)
from .version import _

log = logging.getLogger()

__all__ = ['catSdk']


def load_ccat():
    path = os.path.dirname(os.path.abspath(__file__))
    if 'Linux' in platform.system():
        if platform.libc_ver()[0] == 'glibc':
            return ffi.dlopen(os.path.join(path, "lib/linux-glibc/libcatclient.so"))
        else:
            return ffi.dlopen(os.path.join(path, "lib/linux-musl-libc/libcatclient.so"))
    elif 'Darwin' in platform.system():
        return ffi.dlopen(os.path.join(path, "lib/darwin/libcatclient.dylib"))
    else:
        log.error("pycat can only run on the Linux/Darwin platform.")
    return


ccat = load_ccat()


class PyTransaction(object):

    def __init__(self, mtype, mname):
        self._mtype = mtype
        self._mname = mname
        self._status = CAT_SUCCESS
        self._data = ""
        self._duration = None
        self._time = int(time.time() * 1000)
        self._start = self._time

    def setStatus(self, t, status):
        self._status = status

    def setTimestamp(self, t, timestamp):
        self._time = timestamp

    def setDurationInMillis(self, t, duration):
        self._duration = duration

    def setDurationStart(self, t, durationStart):
        self._start = durationStart

    def addData(self, t, data):
        if self._data == "":
            self._data = data
        else:
            self._data += "&" + data

    def addKV(self, t, key, val):
        if self._data == "":
            self._data = "{}={}".format(key, val)
        else:
            self._data += "&{}={}".format(key, val)

    def complete(self, t):
        t = ccat.newTransaction(_(self._mtype), _(self._mname))
        try:
            t.setStatus(t, _(self._status))
            t.setTimestamp(t, self._time)
            t.setDurationInMillis(t, self._duration_ms)
            t.addData(t, _(self._data))
        finally:
            t.complete(t)

    @property
    def _duration_ms(self):
        if self._duration is None:
            return int(time.time() * 1000 - self._start)
        return self._duration


class catSdk(object):

    def __init__(self, appkey, **kwargs):
        self.appkey = appkey
        self.__init_ccat(**kwargs)

    def __init_ccat(self, encoder=ENCODER_BINARY, sampling=True, debug=False, logview=True, auto_init=False):
        self._logview = logview
        config = ffi.new("CatClientConfig*", [
            encoder,                # encoder
            0,                      # heartbeat
            int(sampling),          # sampling
            1,                      # multiprocessing
            1 if debug else 0,      # debug log
            1 if auto_init else 0,  # auto initialize
        ])
        ccat.catClientInitWithConfig(_(self.appkey), config)

    def new_transaction(self, type, name):
        if not self._logview:
            return PyTransaction(type, name)
        return ccat.newTransaction(_(type), _(name))

    def new_heartbeat(self, type, name):
        return ccat.newHeartBeat(_(type), _(name))

    def log_event(self, mtype, mname, status, nameValuePairs):
        ccat.logEvent(_(mtype), _(mname), _(status), _(nameValuePairs))

    def log_error(self, msg, err_stack):
        ccat.logError(_(msg), _(err_stack))

    def log_metric_for_count(self, name, count=1):
        ccat.logMetricForCount(_(name), count)

    def log_metric_for_duration(self, name, duration_ms):
        ccat.logMetricForDuration(_(name), duration_ms)


class catSdkCoroutine(catSdk):
    pass
