package com.dianping.cat.report.chart.impl;

import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.chart.MetricDataFetcher;

public class MetricDataFetcherImpl implements MetricDataFetcher {


	@Override
	public Map<String, double[]> buildAllGraphData(MetricReport metricReport) {
		AllMetricDataBuilder builder = new AllMetricDataBuilder();

		builder.visitMetricReport(metricReport);
		Map<String, double[]> datas = builder.getDatas();
		return datas;
	}

	@Override
   public Map<String, double[]> buildLeastGraphData(MetricReport metricReport) {
	   LeastMetricDataBuilder builder = new LeastMetricDataBuilder();
	   
	   builder.visitMetricReport(metricReport);
	   Map<String, double[]> datas = builder.getDatas();
	   return datas;
   }
	
	

}
