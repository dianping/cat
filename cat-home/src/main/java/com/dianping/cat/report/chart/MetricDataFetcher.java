package com.dianping.cat.report.chart;

import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;

public interface MetricDataFetcher {
	public Map<String, double[]> buildAllGraphData(MetricReport report);
	
	public Map<String, double[]> buildLeastGraphData(MetricReport report);
}
