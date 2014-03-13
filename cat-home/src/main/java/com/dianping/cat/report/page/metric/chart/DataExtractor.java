package com.dianping.cat.report.page.metric.chart;

import java.util.Map;

public interface DataExtractor {

	public double[] extract(double[] values);

	public Map<String, double[]> extract(final Map<String, double[]> values);

	public int getStep();
}
