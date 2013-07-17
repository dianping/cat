package com.dianping.cat.report.page.metric;

import java.util.ArrayList;
import java.util.Date;
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
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.system.page.abtest.service.ABTestService;

public class MetricDisplay extends BaseVisitor {

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> m_abtests = new HashMap<Integer, com.dianping.cat.home.dal.abtest.Abtest>();

	private String m_abtest;

	private Date m_start;

	private String m_metricKey;

	private String m_currentComputeType;

	private ABTestService m_abtestService;

	private static final String SUM = CatString.SUM;

	private static final String COUNT = CatString.COUNT;

	private static final String AVG = CatString.AVG;

	public List<LineChart> getLineCharts() {
		return new ArrayList<LineChart>(m_lineCharts.values());
	}

	public Map<Integer,com.dianping.cat.home.dal.abtest.Abtest> getAbtests() {
		return m_abtests;
	}

	public MetricDisplay(List<MetricItemConfig> configs, String abtest, Date start) {
		m_start = start;
		m_abtest = abtest;

		for (MetricItemConfig config : configs) {
			if (config.isShowSum()) {
				String key = config.getId() + SUM;

				m_lineCharts.put(key, creatLineChart(config.getTitle() + CatString.Suffix_SUM));
			}
			if (config.isShowCount()) {
				String key = config.getId() + COUNT;

				m_lineCharts.put(key, creatLineChart(config.getTitle() + CatString.Suffix_COUNT));
			}
			if (config.isShowAvg()) {
				String key = config.getId() + AVG;

				m_lineCharts.put(key, creatLineChart(config.getTitle() + CatString.Suffix_AVG));
			}
		}
	}

	private LineChart creatLineChart(String title) {
		LineChart lineChart = new LineChart();

		lineChart.setTitle(title);
		lineChart.setStart(m_start);
		lineChart.setSize(60);
		lineChart.setStep(TimeUtil.ONE_MINUTE);
		return lineChart;
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

	private LineChart findOrCreateChart(String type, String metricKey, String computeType) {
		String key = metricKey + computeType;
		LineChart chart = m_lineCharts.get(key);

		if (chart == null) {
			if (computeType.equals(COUNT)) {
				if (type.equals("C") || type.equals("S,C")) {
					chart = creatLineChart(key);
				}
			} else if (computeType.equals(AVG)) {
				if (type.equals("T")) {
					chart = creatLineChart(key);
				}
			} else if (computeType.equals(SUM)) {
				if (type.equals("S") || type.equals("S,C")) {
					chart = creatLineChart(key);
				}
			}

			if (chart != null) {
				m_lineCharts.put(key, chart);
			}
		}

		return chart;
	}

	@Override
	public void visitGroup(Group group) {
		String id = group.getName();

		if ("".equals(id)) {
			id = "Default";
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

		LineChart sumLine = findOrCreateChart(m_currentComputeType, m_metricKey, SUM);

		if (sumLine != null) {
			sumLine.addSubTitle(id);
			sumLine.addValue(sum);
		}
		LineChart countLine = findOrCreateChart(m_currentComputeType, m_metricKey, COUNT);

		if (countLine != null) {
			countLine.addSubTitle(id);
			countLine.addValue(count);
		}
		LineChart avgLine = findOrCreateChart(m_currentComputeType, m_metricKey, AVG);

		if (avgLine != null) {
			avgLine.addSubTitle(id);
			avgLine.addValue(avg);
		}
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_metricKey = metricItem.getId();
		m_currentComputeType = metricItem.getType();
		super.visitMetricItem(metricItem);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

	public void setAbtest(ABTestService service) {
		m_abtestService = service;
	}

	private com.dianping.cat.home.dal.abtest.Abtest findAbTest(int id) {
		com.dianping.cat.home.dal.abtest.Abtest abtest = m_abtestService.getABTestNameByRunId(id);

		if (abtest == null) {
			abtest = new com.dianping.cat.home.dal.abtest.Abtest();

			abtest.setId(id);
			abtest.setName(String.valueOf(id));
		}

		return abtest;
	}

}
