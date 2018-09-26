#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import logging
import os
import platform

from .catffi import ffi
from .const import (
    ENCODER_BINARY
)
from .version import _

log = logging.getLogger()

__all__ = ['catSdk']


class catSdk(object):

    def __init__(self, appkey, **kwargs):
        path = os.path.dirname(os.path.abspath(__file__))
        if 'Linux' in platform.system():
            self.cat = ffi.dlopen(
                os.path.join(path, "lib/linux/libcatclient.so")
            )
        elif 'Darwin' in platform.system():
            self.cat = ffi.dlopen(
                os.path.join(path, "lib/darwin/libcatclient.dylib")
            )
        else:
            log.error("pycat can only run on the Linux/Darwin platform.")
            return

        self.appkey = appkey
        self.__init_cat_client(**kwargs)

    def __init_cat_client(self, encoder=ENCODER_BINARY, sampling=True, debug=False):
        config = ffi.new("CatClientConfig*", [
            encoder,            # use binary encoder
            0,                  # disable heartbeat
            int(sampling),      # enable sampling
            1,                  # enable multiprocessing
            1 if debug else 0,  # enable debug log
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
