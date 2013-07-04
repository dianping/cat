//package com.dianping.cat.report.task.metric;
//
//import com.dianping.cat.consumer.metric.model.entity.Metric;
//import com.dianping.cat.consumer.metric.model.entity.MetricReport;
//import com.dianping.cat.consumer.metric.model.entity.Point;
//import com.dianping.cat.consumer.metric.model.transform.DefaultMerger;
//
//public class MetricReportMerger extends DefaultMerger {
//
//	private double m_weight;
//
//	public double getWeight() {
//		return m_weight;
//	}
//
//	public void setWeight(double weight) {
//		m_weight = weight;
//	}
//
//	public MetricReportMerger(MetricReport metricReport) {
//		super(metricReport);
//
//	}
//
//	@Override
//	protected void mergeMetric(Metric old, Metric metric) {
//		super.mergeMetric(old, metric);
//	}
//
//	@Override
//	protected void mergePoint(Point old, Point point) {
//		long count = old.getCount();
//		if (count == 0) {
//			old.setCount(old.getCount() + point.getCount());
//			old.setSum(old.getSum() + point.getSum());
//			if (old.getCount() > 0) {
//				old.setAvg(old.getSum() / old.getCount());
//			}
//		} else {
//			old.setCount((long) ((count * m_weight + point.getCount()) / (m_weight + 1)));
//			old.setSum((old.getSum() * m_weight + point.getSum()) / (m_weight + 1));
//			if (old.getCount() > 0) {
//				old.setAvg(old.getSum() / old.getCount());
//			}
//		}
//	}
//
//	@Override
//	public void visitMetricReport(MetricReport metricReport) {
//		MetricReport report = getMetricReport();
//		report.getGroupNames().addAll(metricReport.getGroupNames());
//		super.visitMetricReport(metricReport);
//	}
//
//}
