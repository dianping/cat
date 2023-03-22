package com.dianping.cat.message;

import java.util.Collection;

public interface MetricBag {
	String getDomain();

	String getHostName();

	String getIpAddress();

	Collection<Metric> getMetrics();
}
