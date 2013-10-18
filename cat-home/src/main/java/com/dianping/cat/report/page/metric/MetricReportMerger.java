package com.dianping.cat.report.page.metric;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.task.metric.MetricType;

public class MetricReportMerger extends BaseVisitor {
	private Map<String, Map<String, double[][]>> m_metricStatistic = new LinkedHashMap<String, Map<String, double[][]>>();

	private String m_abtest;

	private String m_subtitle;

	private String m_metricKey;

	private String m_currentComputeType;

	private int m_index;

	private static final String SUM = MetricType.SUM.name();

	private static final String COUNT = MetricType.COUNT.name();

	private static final String AVG = MetricType.AVG.name();

	public MetricReportMerger(String abtest, String subtitle) {
		m_abtest = abtest;
		m_subtitle = subtitle;
	}

	private Map<String, double[][]> findOrCreateStatistic(String type, String metricKey, String computeType) {
		String key = metricKey + ":" + computeType;
		Map<String, double[][]> statisticItem = m_metricStatistic.get(key);

		if (statisticItem == null) {
			if (computeType.equals(COUNT)) {
				if (type.equals("C") || type.equals("S,C")) {
					statisticItem = createMetricStatistic(key);
				}
			} else if (computeType.equals(AVG)) {
				if (type.equals("T")) {
					statisticItem = createMetricStatistic(key);
				}
			} else if (computeType.equals(SUM)) {
				if (type.equals("S") || type.equals("S,C")) {
					statisticItem = createMetricStatistic(key);
				}
			}
			if (statisticItem != null) {
				m_metricStatistic.put(key, statisticItem);
			}
		}
		return statisticItem;
	}

	private Map<String, double[][]> createMetricStatistic(String key) {
		Map<String, double[][]> value = new LinkedHashMap<String, double[][]>();
		m_metricStatistic.put(key, value);
		return value;
	}

	@Override
	public void visitAbtest(Abtest abtest) {
		String abtestId = abtest.getRunId();
		if (m_abtest.equals(abtestId)) {
			super.visitAbtest(abtest);
		}
	}

	@Override
	public void visitGroup(Group group) {
		String id = group.getName();

		if ("".equals(id)) {
			id = m_subtitle;
		}

		double[] sum = new double[60];
		double[] avg = new double[60];
		double[] count = new double[60];

		for (Point point : group.getPoints().values()) {
			int index = point.getId();

			sum[index] = point.getSum();
			avg[index] = point.getAvg();
			count[index] = point.getCount();
		}

		Map<String, double[][]> sumLines = findOrCreateStatistic(m_currentComputeType, m_metricKey, SUM);

		if (sumLines != null) {
			double[][] sumLine = findOrCreateLine(sumLines, id);
			sumLine[m_index] = sum;
		}

		Map<String, double[][]> countLines = findOrCreateStatistic(m_currentComputeType, m_metricKey, COUNT);

		if (countLines != null) {
			double[][] countLine = findOrCreateLine(countLines, id);
			countLine[m_index] = count;
		}

		Map<String, double[][]> avgLines = findOrCreateStatistic(m_currentComputeType, m_metricKey, AVG);

		if (avgLines != null) {
			double[][] avgLine = findOrCreateLine(avgLines, id);
			avgLine[m_index] = avg;
		}
	}

	private double[][] findOrCreateLine(Map<String, double[][]> lines, String id) {
		double[][] result = lines.get(id);
		if (result == null) {
			result = new double[24][];
			lines.put(id, result);
		}
		return result;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_metricKey = metricItem.getId();
		m_currentComputeType = metricItem.getType();
		super.visitMetricItem(metricItem);
	}

	public void visitMetricReport(int index, MetricReport report) {
		m_index = index;
		visitMetricReport(report);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

	public Map<String, Map<String, double[][]>> getMetricStatistic() {
		return m_metricStatistic;
	}

}
