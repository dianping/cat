package com.dianping.cat.report.page.model.metric;

import com.dianping.cat.consumer.metric.model.entity.Metric;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.DefaultMerger;

public class MetricReportMerger extends DefaultMerger {

	public MetricReportMerger(MetricReport metricReport) {
		super(metricReport);

	}

	@Override
	protected void mergeMetric(Metric old, Metric metric) {
		super.mergeMetric(old, metric);
	}

	@Override
	protected void mergePoint(Point old, Point point) {
		old.setCount(old.getCount() + point.getCount());
		old.setSum(old.getSum() + point.getSum());
		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}
	

	@Override
   public void visitMetricReport(MetricReport metricReport) {
		MetricReport report = getMetricReport();
		report.getGroupNames().addAll(metricReport.getGroupNames());
	   super.visitMetricReport(metricReport);
   }
	
}
