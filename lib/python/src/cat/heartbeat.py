#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import socket
import os.path

from .const import CAT_SUCCESS
from .message import NullMessage
from .container import sdk

from .event import log_exception

from .utils import genXml


class Heartbeat(NullMessage):

    def __init__(self, mtype, mname):
        self._hb = sdk().new_heartbeat(mtype, mname)
        self._completed = False

    def complete(self):
        if not self._completed:
            self._hb.complete(self._hb)
            self._completed = True
        return self

    def set_status(self, status):
        self._hb.setStatus(self._hb, status)
        return self

    def add_data(self, data):
        self._hb.addData(self._hb, data)
        return self


def _collect_load_avg():
    if not os.path.isfile("/proc/loadavg"):
        return 0, 0, 0
    with open("/proc/loadavg") as f:
        s = f.read().split()
        load_1min = s[0]
        load_5min = s[1]
        load_15min = s[2]
        return load_1min, load_5min, load_15min


def _collect():
    wx = genXml()
    wx.set_tree_tag("status")
    wx.set_child_tag("extension", "system info")
    d_json = dict()

    load_1, load_5, load_15 = _collect_load_avg()
    d_json["system_avg_load"] = load_1

    wx.set_data_graph('extensionDetail', **d_json)
    result = wx.display()
    return result


def __get_local_ip():
    try:
        fqdn = socket.getfqdn(socket.gethostname())
        return socket.gethostbyname(fqdn)
    except Exception as e:
        log_exception(e)
        return "10.10.10.2"


def _build_heartbeat():
    hb = Heartbeat("Heartbeat", __get_local_ip())
    data = _collect()
    hb.add_data(data)
    hb.set_status(CAT_SUCCESS)
    hb.complete()
    return hb
