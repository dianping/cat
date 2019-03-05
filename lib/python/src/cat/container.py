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

import logging

log = logging.getLogger()


class Blackhole(object):

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
        return Blackhole()
    return x
