package com.dianping.cat.message.context;

public class MetricContextHelper {
	private static MetricContext s_context = new DefaultMetricContext();

	public static MetricContext context() {
		return s_context;
	}
}
