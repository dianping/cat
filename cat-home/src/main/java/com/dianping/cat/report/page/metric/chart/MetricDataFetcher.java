package com.dianping.cat.report.page.metric.chart;

import java.util.List;
import java.util.Map;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;

public interface MetricDataFetcher {
	public Map<String, double[]> buildGraphData(MetricReport report, List<MetricItemConfig> metricConfigs,
	      String abtestId);
}
