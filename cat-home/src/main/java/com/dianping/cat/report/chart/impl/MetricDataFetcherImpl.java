package com.dianping.cat.report.chart.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.chart.MetricDataFetcher;
import com.dianping.cat.report.task.alert.MetricType;

public class MetricDataFetcherImpl implements MetricDataFetcher {

	private final String SUM = MetricType.SUM.name();

	private final String COUNT = MetricType.COUNT.name();

	private final String AVG = MetricType.AVG.name();

	@Override
	public Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		MetricDataBuilder builder = new MetricDataBuilder();

		builder.visitMetricReport(metricReport);
		Map<String, double[]> datas = builder.getDatas();
		return datas;
	}

	public class MetricDataBuilder extends BaseVisitor {
		private Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

		private String m_metricKey;

		private String m_currentComputeType;

		public MetricDataBuilder() {
		}

		private double[] findOrCreateStatistic(String type, String metricKey, String computeType) {
			String key = metricKey + ":" + computeType;
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
			m_currentComputeType = metricItem.getType();

			double[] sum = findOrCreateStatistic(m_currentComputeType, m_metricKey, SUM);
			double[] count = findOrCreateStatistic(m_currentComputeType, m_metricKey, COUNT);
			double[] avg = findOrCreateStatistic(m_currentComputeType, m_metricKey, AVG);

			for (Segment seg : metricItem.getSegments().values()) {
				int index = seg.getId();

				sum[index] = seg.getSum();
				avg[index] = seg.getAvg();
				count[index] = seg.getCount();
			}
		}

		public void visitMetricReport(int index, MetricReport report) {
			visitMetricReport(report);
		}

		@Override
		public void visitMetricReport(MetricReport metricReport) {
			super.visitMetricReport(metricReport);
		}
	}

}
