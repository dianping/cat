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

import traceback

from .const import CAT_SUCCESS
from .container import sdk

__all__ = ["log_event", "log_exception", "log_error"]


def log_event(mtype, mname, status=CAT_SUCCESS, data=""):
    sdk().log_event(mtype, mname, status, data)


def log_exception(exc, err_stack=None):
    if err_stack is None:
        err_stack = traceback.format_exc()
    sdk().log_error(exc.__class__.__name__, err_stack)


def log_error(err_message, err_stack=None):
    if err_stack is None:
        err_stack = traceback.format_exc()
    sdk().log_error(err_message, err_stack)
