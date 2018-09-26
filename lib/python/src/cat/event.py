#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

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
