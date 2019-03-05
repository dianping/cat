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

#!/usr/bin/env python
# -*- coding: utf-8 -*-

import functools
import logging
import threading
import time

from .version import PY2

from xml.dom.minidom import Document

log = logging.getLogger(__name__)


def synchronized(func):
    func.__lock = threading.Lock()

    @functools.wraps(func)
    def wraps(*args, **kwargs):
        with func.__lock:
            return func(*args, **kwargs)
    return wraps


def singleton(thread_safe=False):

    instances = {}

    def wrapper(cls):
        def get_instance(*args, **kwargs):
            if cls not in instances:
                instances[cls] = cls(*args, **kwargs)
            return instances[cls]

        if thread_safe:
            return synchronized(get_instance)
        else:
            return get_instance
    return wrapper


def deprecated(message):
    def wrapped(func):
        @functools.wraps(func)
        def wraps(*args, **kwargs):
            log.warning("{0} has been deprecated, {1}".format(
                func.__name__, message))
            return func(*args, **kwargs)
        return wraps
    return wrapped


def sleep_2_next_min():
    next_tick = 60 * (int(time.time() / 60) + 1)
    delta = next_tick - time.time()
    if delta > 0:
        time.sleep(delta)


class genXml(Document):
    """
        called:
            wx = gen_xml()
            wx.set_tree_tag("status")
            wx.set_child_tag("extension", "system info")
            d_json = {"cpu.load": '0.99', 'load.avg': '1.24'}
            wx.set_data_graph('extensionDetail', **d_json)
            wx.display()
        result:
            <?xml version="1.0" ?>
            <status>
              <extension id="system info">
                <extensionDetail id="cpu.load" value="0.99"/>
                <extensionDetail id="load.avg" value="1.24"/>
              </extension>
            </status>
    """

    def __init__(self):
        Document.encoding = 'utf-8'
        Document.__init__(self)
        self.tree = None
        self.child_tree = None
        self.graph = None

    def set_tree_tag(self, tag):
        self.tree = self.createElement(tag)
        self.appendChild(self.tree)

    def set_child_tag(self, child_name, child_id):
        self.child_tree = self.createElement(child_name)
        self.child_tree.setAttribute("id", child_id)
        self.tree.appendChild(self.child_tree)

    def set_data_graph(self, graph_name, **kwargs):
        iterator = kwargs.iteritems() if PY2 else kwargs.items()
        for k, v in iterator:
            self.graph = self.createElement(graph_name)
            self.graph.setAttribute('id', k)
            self.graph.setAttribute('value', str(v))
            self.child_tree.appendChild(self.graph)

    def display(self):
        return self.toprettyxml(indent="  ")
