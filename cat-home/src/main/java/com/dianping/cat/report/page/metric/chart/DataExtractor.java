package com.dianping.cat.report.page.metric.chart;

import java.util.Map;

public interface DataExtractor {

	public double[] extractor(double[] values);

	public Map<String, double[]> extractor(Map<String, double[]> values);

	public int getStep();
}
