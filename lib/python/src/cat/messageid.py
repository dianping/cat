#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: litong
# Email: <litonglitong@hotmail.com>

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

__all__ = ["create_message_id", "create_remote_server_message_id", "get_thread_local_message_tree_id",
           "get_thread_local_message_tree_root_id", "get_thread_local_message_tree_parent_id",
           "set_thread_local_message_tree_id", "set_thread_local_message_tree_root_id", "set_thread_local_message_tree_parent_id"]


def create_message_id():
    return sdk().create_message_id()

def create_remote_server_message_id(appKey):
    return sdk().create_remote_server_message_id(appKey)

def get_thread_local_message_tree_id():
    return sdk().get_thread_local_message_tree_id()

def get_thread_local_message_tree_root_id():
    return sdk().get_thread_local_message_tree_root_id()

def get_thread_local_message_tree_parent_id():
    return sdk().get_thread_local_message_tree_parent_id()

def set_thread_local_message_tree_id(message_id):
    sdk().set_thread_local_message_tree_id(message_id)

def set_thread_local_message_tree_root_id(message_id):
    sdk().set_thread_local_message_tree_root_id(message_id)

def set_thread_local_message_tree_parent_id(message_id):
    sdk().set_thread_local_message_tree_parent_id(message_id)


