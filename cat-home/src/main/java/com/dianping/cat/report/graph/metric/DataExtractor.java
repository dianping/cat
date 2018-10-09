package com.dianping.cat.report.graph.metric;

import java.util.Map;

public interface DataExtractor {

	public int calculateInterval(int length);

	public double[] extract(double[] values);

	public Map<String, double[]> extract(final Map<String, double[]> values);

	public int getStep();
}