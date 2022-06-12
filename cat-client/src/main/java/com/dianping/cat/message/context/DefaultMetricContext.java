package com.dianping.cat.message.context;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.internal.DefaultMetric;

public class DefaultMetricContext implements MetricContext {
	@Override
	public Metric newMetric(String name) {
		return new DefaultMetric(this, name);
	}

	@Override
	public void add(Metric metric) {
		// TODO Auto-generated method stub
		
	}
}
