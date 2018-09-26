#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

from .container import sdk


__all__ = ['MetricHelper', 'metric']


class MetricHelper(object):

    def __init__(self, name):
        self._name = name

    def add_name(self, name):
        self._name = name
        return self

    def count(self, count=1):
        sdk().log_metric_for_count(self._name, count)

    def duration(self, duration_ms):
        sdk().log_metric_for_duration(self._name, duration_ms)


def metric(name):
    return MetricHelper(name)
