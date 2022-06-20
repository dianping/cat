package com.dianping.cat.message.internal;

import java.util.Collection;

import com.dianping.cat.message.Metric;

public interface MetricBag {
	String getDomain();

	String getHostName();

	String getIpAddress();

	Collection<Metric> getMetrics();
}
