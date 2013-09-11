package com.dianping.cat.report.page.metric;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.system.page.abtest.service.ABTestService;

public class MetricDisplayMerger extends BaseVisitor {

	private Map<String, Map<String, double[][]>> m_metricStatistic = new LinkedHashMap<String, Map<String, double[][]>>();

	private Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> m_abtests = new HashMap<Integer, com.dianping.cat.home.dal.abtest.Abtest>();

	private String m_abtest;

	private String m_metricKey;

	private String m_currentComputeType;

	private ABTestService m_abtestService;

	private int m_index;

	private static final String SUM = MetricType.SUM.name();

	private static final String COUNT = MetricType.COUNT.name();

	private static final String AVG = MetricType.AVG.name();

	private boolean m_isDashboard;
	
	public MetricDisplayMerger(String abtest, boolean isDashboard) {
		m_abtest = abtest;
		m_isDashboard = isDashboard;
	}

	private Map<String, double[][]> findOrCreateStatistic(String type, String metricKey, String computeType) {
		String key = metricKey + ":" + computeType;
		Map<String, double[][]> statisticItem = m_metricStatistic.get(key);

		if (statisticItem == null && !m_isDashboard) {
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
	
	public MetricDisplayMerger setAbtestService(ABTestService service) {
		m_abtestService = service;
		return this;
	}

	private Map<String, double[][]> createMetricStatistic(String key) {
		Map<String, double[][]> value = new LinkedHashMap<String, double[][]>();
		m_metricStatistic.put(key, value);
		return value;
	}

	private com.dianping.cat.home.dal.abtest.Abtest findAbTest(int id) {
		com.dianping.cat.home.dal.abtest.Abtest abtest = null;
		if (id >= 0) {
			abtest = m_abtestService.getABTestNameByRunId(id);
		}
		if (abtest == null) {
			abtest = new com.dianping.cat.home.dal.abtest.Abtest();

			abtest.setId(id);
			abtest.setName(String.valueOf(id));
		}
		return abtest;
	}

	@Override
	public void visitAbtest(Abtest abtest) {
		String abtestId = abtest.getRunId();
		int id = Integer.parseInt(abtestId);
		com.dianping.cat.home.dal.abtest.Abtest temp = findAbTest(id);

		m_abtests.put(id, temp);
		if (m_abtest.equals(abtestId)) {
			super.visitAbtest(abtest);
		}
	}

	@Override
	public void visitGroup(Group group) {
		String id = group.getName();

		if ("".equals(id)) {
			id = "Current";
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
