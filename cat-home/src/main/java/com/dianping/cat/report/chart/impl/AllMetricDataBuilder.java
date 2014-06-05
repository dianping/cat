package com.dianping.cat.report.chart.impl;


import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.report.task.metric.MetricType;

public class AllMetricDataBuilder extends LeastMetricDataBuilder {

	private final String SUM = MetricType.SUM.name();

	private final String COUNT = MetricType.COUNT.name();

	private final String AVG = MetricType.AVG.name();

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_metricKey = metricItem.getId();

		double[] sum = findOrCreateStatistic(m_metricKey + ":" + SUM);
		double[] count = findOrCreateStatistic(m_metricKey + ":" + COUNT);
		double[] avg = findOrCreateStatistic(m_metricKey + ":" + AVG);

		for (Segment seg : metricItem.getSegments().values()) {
			int index = seg.getId();

			sum[index] = seg.getSum();
			avg[index] = seg.getAvg();
			count[index] = seg.getCount();
		}
	}
}
