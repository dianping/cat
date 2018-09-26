#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

import cffi
import sys

PY2 = False
PY3 = False

if (sys.version_info > (3, 0)):
    PY3 = True
else:
    PY2 = True

if PY3:
    def _(x):
        return cffi.new("char[]", x.encode("utf-8"))
else:
    def _(x):
        return x.encode('utf-8') if isinstance(x, unicode) else x
