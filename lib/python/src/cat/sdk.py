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
    ENCODER_BINARY,
    CAT_SUCCESS
)
from .version import _

log = logging.getLogger()

__all__ = ['catSdk']


class catSdk(object):

    def __init__(self, appkey, **kwargs):
        path = os.path.dirname(os.path.abspath(__file__))
        if 'Linux' in platform.system():
            if platform.libc_ver()[0] == 'glibc':
                self.cat = ffi.dlopen(
                    os.path.join(path, "lib/linux-glibc/libcatclient.so")
                )
            else:
                self.cat = ffi.dlopen(
                    os.path.join(path, "lib/linux-musl-libc/libcatclient.so")
                )
        elif 'Darwin' in platform.system():
            self.cat = ffi.dlopen(
                os.path.join(path, "lib/darwin/libcatclient.dylib")
            )
        else:
            log.error("pycat can only run on the Linux/Darwin platform.")
            return

        self.appkey = appkey
        self.__init_ccat(**kwargs)

    def __init_ccat(self, encoder=ENCODER_BINARY, sampling=True, debug=False, logview=True):
        config = ffi.new("CatClientConfig*", [
            encoder,            # encoder
            0,                  # heartbeat
            int(sampling),      # sampling
            1,                  # multiprocessing
            1 if debug else 0,  # debug log
        ])
        self.cat.catClientInitWithConfig(_(self.appkey), config)

    def new_transaction(self, type, name):
        return self.cat.newTransaction(_(type), _(name))

    def new_heartbeat(self, type, name):
        return self.cat.newHeartBeat(_(type), _(name))

    def log_event(self, mtype, mname, status, nameValuePairs):
        self.cat.logEvent(_(mtype), _(mname), _(status), _(nameValuePairs))

    def log_error(self, msg, err_stack):
        self.cat.logError(_(msg), _(err_stack))

    def log_metric_for_count(self, name, count=1):
        self.cat.logMetricForCount(_(name), count)

    def log_metric_for_duration(self, name, duration_ms):
        self.cat.logMetricForDuration(_(name), duration_ms)

    def _add_transaction_data(self, t, data):
        '''
        It's a temporary api, don't use it in your code!
        '''
        t.addData(t, _(data))

    def _add_transaction_kv(self, t, key, val):
        '''
        It's a temporary api, don't use it in your code!
        '''
        t.addKV(t, _(key), _(val))


class catSdkCoroutine(catSdk):
    '''
    This is a wrapper of catSdk.
    We don't create a ccat Transaction struct but a pycat Transaction instead.
    All the properties are cached in the pycat object.
    '''

    class Transaction:

        def __init__(self, sdk, mtype, mname):
            self._sdk = sdk
            self._type = _(mtype)
            self._name = _(mname)
            self._status = _(CAT_SUCCESS)
            self._data = ""
            self._timestamp = time.time() * 1000
            self._duration = None
            self._duration_start = self._timestamp

        @property
        def type(self):
            return self._type

        @property
        def name(self):
            return self._name

        @property
        def status(self):
            return self._status

        @property
        def data(self):
            return self._data

        def setStatus(self, t, status):
            self._status = status

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

        def setDurationInMillis(self, t, duration):
            self._duration = duration

        def setDurationStart(self, t, timestamp):
            self._duration_start = timestamp

        def setTimestamp(self, t, timestamp):
            self._timestamp = timestamp

        def complete(self, trans):
            if self._duration is None:
                duration = time.time() * 1000 - self._duration_start
            else:
                duration = self._duration

            t = self._sdk.cat.newTransaction(self.type, self.name)
            t.setStatus(t, self.status)
            t.setTimestamp(t, int(self._timestamp))
            t.setDurationInMillis(t, int(duration))
            t.addData(t, _(self.data))
            t.complete(t)

    def new_transaction(self, type, name):
        return self.Transaction(self, type, name)

    def _add_transaction_data(self, t, data):
        '''
        It's a temporary api, don't use it in your code!
        '''
        t.addData(t, data)

    def _add_transaction_kv(self, t, key, val):
        '''
        It's a temporary api, don't use it in your code!
        '''
        t.addKV(t, key, val)
