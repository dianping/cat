package com.dianping.cat.message.context;

import com.dianping.cat.message.Metric;

public interface MetricContext {
	String TICK = "__TICK__";
	
	Metric newMetric(String name);

	void add(Metric metric);
	
	void tick();
}
