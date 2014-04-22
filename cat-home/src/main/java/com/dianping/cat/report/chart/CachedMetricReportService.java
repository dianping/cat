package com.dianping.cat.report.chart;

import java.util.Date;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;

public interface CachedMetricReportService {

	public MetricReport query(String product, Date date);

}
