package com.dianping.cat.message.context;

import com.dianping.cat.message.Metric;

public interface MetricContext {
	Metric newMetric(String name);

	void add(Metric metric);
}
