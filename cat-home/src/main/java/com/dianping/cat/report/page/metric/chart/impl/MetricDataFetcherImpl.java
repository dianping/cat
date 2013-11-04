package com.dianping.cat.report.page.metric.chart.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.page.metric.chart.MetricDataFetcher;
import com.dianping.cat.report.task.metric.MetricType;

public class MetricDataFetcherImpl implements MetricDataFetcher {

	private final String SUM = MetricType.SUM.name();

	private final String COUNT = MetricType.COUNT.name();

	private final String AVG = MetricType.AVG.name();

	@Override
	public Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs,
	      String abtestId) {
		MetricDataBuilder builder = new MetricDataBuilder(abtestId);
		
		builder.visitMetricReport(metricReport);
		Map<String, double[]> datas = builder.getDatas();
		Map<String, double[]> values = new HashMap<String, double[]>();

		for (MetricItemConfig config : metricConfigs) {
			String key = config.getId();

			if (config.getShowAvg()) {
				String avgKey = key + ":" + AVG;
				putKey(datas, values, avgKey);
			}
			if (config.getShowCount()) {
				String countKey = key + ":" + COUNT;
				putKey(datas, values, countKey);
			}
			if (config.getShowSum()) {
				String sumKey = key + ":" + SUM;
				putKey(datas, values, sumKey);
			}
		}
		return values;
	}

	private void putKey(Map<String, double[]> datas, Map<String, double[]> values, String key) {
	   double[] value = datas.get(key);
	   
	   if(value ==null){
	   	value = new double[60];
	   }
	   values.put(key, value);
   }

	public class MetricDataBuilder extends BaseVisitor {
		private Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

		private String m_abtestId;

		private String m_metricKey;

		private String m_currentComputeType;

		public MetricDataBuilder(String abtest) {
			m_abtestId = abtest;
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
		public void visitAbtest(Abtest abtest) {
			String abtestId = abtest.getRunId();
			if (m_abtestId.equals(abtestId)) {
				super.visitAbtest(abtest);
			}
		}

		@Override
		public void visitGroup(Group group) {
			double[] sum = findOrCreateStatistic(m_currentComputeType, m_metricKey, SUM);
			double[] count = findOrCreateStatistic(m_currentComputeType, m_metricKey, COUNT);
			double[] avg = findOrCreateStatistic(m_currentComputeType, m_metricKey, AVG);

			for (Point point : group.getPoints().values()) {
				int index = point.getId();

				sum[index] = point.getSum();
				avg[index] = point.getAvg();
				count[index] = point.getCount();
			}
		}

		@Override
		public void visitMetricItem(MetricItem metricItem) {
			m_metricKey = metricItem.getId();
			m_currentComputeType = metricItem.getType();
			super.visitMetricItem(metricItem);
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
