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
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.system.page.abtest.service.ABTestService;

public class MetricDisplay extends BaseVisitor {

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> m_abtests = new HashMap<Integer, com.dianping.cat.home.dal.abtest.Abtest>();

	private String m_abtest;

	private Date m_start;

	private String m_metricKey;

	private String m_currentComputeType;

	private ABTestService m_abtestService;

	private BaselineService m_baselineService;

	private int m_index;

	private static final String SUM = MetricType.SUM.name();

	private static final String COUNT = MetricType.COUNT.name();

	private static final String AVG = MetricType.AVG.name();

	private static final int INTERVAL = 10;

	private static final int HOUR = 24;

	private static final int MINUTE = 60;

	private static final int POINT_NUM = HOUR * MINUTE / INTERVAL;

	private static final String METRIC_STRING = "metric";

	private boolean m_isDashboard;

	public MetricDisplay(List<MetricItemConfig> configs, String abtest, Date start) {
		this(configs, abtest, start, false);
	}

	private boolean showAvg(MetricItemConfig config) {
		if (m_isDashboard) {
			return config.isShowAvgDashboard() && config.getShowAvg();
		} else {
			return config.getShowAvg();
		}
	}

	private boolean showSum(MetricItemConfig config) {
		if (m_isDashboard) {
			return config.isShowSumDashboard() && config.getShowSum();
		} else {
			return config.getShowSum();
		}
	}

	private boolean showCount(MetricItemConfig config) {
		if (m_isDashboard) {
			return config.isShowCountDashboard() && config.getShowCount();
		} else {
			return config.getShowCount();
		}
	}

	public MetricDisplay(List<MetricItemConfig> configs, String abtest, Date start, boolean isDashboard) {
		m_isDashboard = isDashboard;
		m_start = start;
		m_abtest = abtest;

		for (MetricItemConfig config : configs) {
			String configKey = config.getId();
			if (showSum(config)) {
				String key = configKey + ":" + SUM;
				m_lineCharts.put(key, createLineChart(config.getTitle() + CatString.Suffix_SUM));
			}
			if (showCount(config)) {
				String key = configKey + ":" + COUNT;
				m_lineCharts.put(key, createLineChart(config.getTitle() + CatString.Suffix_COUNT));
			}
			if (showAvg(config)) {
				String key = configKey + ":" + AVG;
				m_lineCharts.put(key, createLineChart(config.getTitle() + CatString.Suffix_AVG));
			}
		}
	}

	private LineChart createLineChart(String title) {
		LineChart lineChart = new LineChart();
		lineChart.setTitle(title);
		lineChart.setStart(m_start);
		lineChart.setSize(POINT_NUM);
		lineChart.setStep(TimeUtil.ONE_MINUTE * INTERVAL);
		return lineChart;
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

	private LineChart findOrCreateChart(String type, String metricKey, String computeType) {
		String key = metricKey + ":" + computeType;
		LineChart chart = m_lineCharts.get(key);

		if (chart == null && !m_isDashboard) {
			if (computeType.equals(COUNT)) {
				if (type.equals("C") || type.equals("S,C")) {
					chart = createLineChart(key);
				}
			} else if (computeType.equals(AVG)) {
				if (type.equals("T")) {
					chart = createLineChart(key);
				}
			} else if (computeType.equals(SUM)) {
				if (type.equals("S") || type.equals("S,C")) {
					chart = createLineChart(key);
				}
			}
			if (chart != null) {
				m_lineCharts.put(key, chart);
			}
		}
		return chart;
	}

	public void generateBaselineChart() {
		for (String key : m_lineCharts.keySet()) {
			LineChart lineChart = m_lineCharts.get(key);
			Date yesterday = TaskHelper.todayZero(m_start);
			int index = (int) ((m_start.getTime() + 8 * TimeUtil.ONE_HOUR) % TimeUtil.ONE_DAY / TimeUtil.ONE_MINUTE);
			double[] yesterdayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, yesterday);
			Date today = TaskHelper.tomorrowZero(m_start);
			double[] todayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, today);
			double[] value = new double[POINT_NUM];
			double[] day = yesterdayBaseline;
			for (int i = 0; i < POINT_NUM; i++) {
				int j = (index + i * INTERVAL) % (HOUR * MINUTE);
				if (j == 0 && index != 0) {
					day = todayBaseline;
				}
				if (day == null) {
					continue;
				}
				value[i] = sumOfArray(day, j);
			}
			lineChart.addSubTitle("Baseline");
			lineChart.addValue(value);
		}
	}

	public void generateDailyLineCharts() {
		for (String key : m_lineCharts.keySet()) {
			LineChart lineChart = m_lineCharts.get(key);
			List<double[]> values = lineChart.getValues();
			List<String> subTitles = lineChart.getSubTitles();
			Map<String, double[]> resultMap = new HashMap<String, double[]>();
			int i = 0;
			for (String subTitle : subTitles) {
				int splitIndex = subTitle.lastIndexOf(':');
				int hour = Integer.parseInt(subTitle.substring(splitIndex + 1));
				subTitle = subTitle.substring(0, splitIndex);
				double[] value = values.get(i);
				double[] newValue = resultMap.get(subTitle);
				if (newValue == null) {
					newValue = new double[POINT_NUM];
					resultMap.put(subTitle, newValue);
				}
				for (int j = 0; j < MINUTE / INTERVAL; j++) {
					newValue[hour * MINUTE / INTERVAL + j] = sumOfArray(value, j * INTERVAL);
				}
				i++;
			}
			values.clear();
			subTitles.clear();
			for (String subTitle : resultMap.keySet()) {
				subTitles.add("Current");
				values.add(resultMap.get(subTitle));
			}
		}
	}

	public Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> getAbtests() {
		return m_abtests;
	}

	public List<LineChart> getLineCharts() {
		return new ArrayList<LineChart>(m_lineCharts.values());
	}

	public MetricDisplay setAbtestService(ABTestService service) {
		m_abtestService = service;
		return this;
	}

	public MetricDisplay setBaselineService(BaselineService baselineService) {
		m_baselineService = baselineService;
		return this;
	}

	private double sumOfArray(double[] values, int j) {
		double result = 0;
		for (int i = j; i < j + INTERVAL; i++) {
			if (values[i] >= 0) {
				result += values[i];
			}
		}
		return result;
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
			id = "Default";
		}

		id = id + ":" + m_index;
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

	public void visitMetricReport(int index, MetricReport report) {
		m_index = index;
		visitMetricReport(report);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

}