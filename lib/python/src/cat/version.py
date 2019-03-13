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

import cffi
import sys

PY2 = False
PY3 = False

if (sys.version_info > (3, 0)):
    PY3 = True
else:
    PY2 = True

if PY3:
    ffi = cffi.FFI()

    def _(x):
        return ffi.new("char[]", x.encode("utf-8"))
else:
    def _(x):
        return x.encode('utf-8') if isinstance(x, unicode) else x
