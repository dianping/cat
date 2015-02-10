package com.dianping.cat.report.graph.metric.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.metric.MetricDataFetcher;
import com.dianping.cat.report.alert.MetricType;

public class MetricDataFetcherImpl implements MetricDataFetcher {

	@Override
	public Map<String, double[]> buildGraphData(MetricReport metricReport) {
		MetricDataBuilder builder = new MetricDataBuilder();

		builder.visitMetricReport(metricReport);
		Map<String, double[]> datas = builder.getDatas();
		return datas;
	}

	public class MetricDataBuilder extends BaseVisitor {

		private final String SUM = MetricType.SUM.name();

		private final String COUNT = MetricType.COUNT.name();

		private final String AVG = MetricType.AVG.name();

		protected Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

		protected String m_metricKey;

		public MetricDataBuilder() {
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

		@Override
		public void visitMetricReport(MetricReport metricReport) {
			super.visitMetricReport(metricReport);
		}
	}
}
