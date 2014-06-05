package com.dianping.cat.report.chart.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;

public class LeastMetricDataBuilder extends BaseVisitor {

	protected Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

	protected String m_metricKey;

	public LeastMetricDataBuilder() {
	}

	protected double[] findOrCreateStatistic(String key) {
		double[] statisticItem = m_datas.get(key);

		if (statisticItem == null) {
			statisticItem = new double[60];
			m_datas.put(key, statisticItem);
		}
		return statisticItem;
	}

	public Map<String, double[]> getDatas() {
		return m_datas;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_metricKey = metricItem.getId();
		double[] data = findOrCreateStatistic(m_metricKey);
		String type = metricItem.getType();

		if ("C".equals(type)) {
			for (Segment seg : metricItem.getSegments().values()) {
				int index = seg.getId();

				data[index] = seg.getSum();
			}
		} else if ("T".equals(type)) {
			for (Segment seg : metricItem.getSegments().values()) {
				int index = seg.getId();

				data[index] = seg.getAvg();
			}
		} else if ("S,C".equals(type)) {
			for (Segment seg : metricItem.getSegments().values()) {
				int index = seg.getId();

				data[index] = seg.getSum();
			}
		}
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}
}
