#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com>

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

__all__ = ['init', 'CAT_SUCCESS', 'CAT_ERROR']

import logging

from .const import *  # noqa

from .utils import (
    synchronized,
)

from .container import container

from .sdk import (
    catSdk as catSdkDefault,
)

log = logging.getLogger()


@synchronized
def init(appkey, **kwargs):
    if container.contains("catsdk"):
        log.warning("cat sdk has already been initialized!")
    sdk = catSdkDefault(appkey, **kwargs)
    container.put("catsdk", sdk)
