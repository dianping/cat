package com.dianping.cat.report.page.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.metric.MetricType;

public class MetricDisplay extends BaseVisitor {

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> m_abtests = new HashMap<Integer, com.dianping.cat.home.dal.abtest.Abtest>();

	private Date m_start;

	private BaselineService m_baselineService;

	private boolean m_isDashboard;

	private MetricDisplayMerger m_displayMerger;

	private int m_timeRange;

	private int m_pointNumber;

	private int m_realPointNumber;

	private int m_interval = 1;

	private static final String SUM = MetricType.SUM.name();

	private static final String COUNT = MetricType.COUNT.name();

	private static final String AVG = MetricType.AVG.name();

	private static final int HOUR = 24;

	private static final int MINUTE = 60;

	private static final String METRIC_STRING = "metric";

	public MetricDisplayMerger getDisplayMerger() {
		return m_displayMerger;
	}

	public void setDisplayMerger(MetricDisplayMerger displayMerger) {
		m_displayMerger = displayMerger;
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

	public MetricDisplay(String abtest, Date start, boolean isDashboard, int timeRange) {
		int minute = (int) (System.currentTimeMillis() % TimeUtil.ONE_HOUR / TimeUtil.ONE_MINUTE);
		m_isDashboard = isDashboard;
		m_start = start;
		m_timeRange = timeRange;
		if (m_timeRange == 24) {
			m_interval = 6;
		} else {
			m_interval = 1;
		}
		m_pointNumber = m_timeRange * MINUTE / m_interval;
		m_realPointNumber = (m_timeRange * MINUTE - 60 + minute) / m_interval;
	}

	public void initializeLineCharts(List<MetricItemConfig> configs) {
		for (MetricItemConfig config : configs) {
			String configKey = config.getId();
			if (showSum(config)) {
				String key = configKey + ":" + SUM;
				m_lineCharts.put(key, createLineChart(config.getTitle() + Chinese.Suffix_SUM));
			}
			if (showCount(config)) {
				String key = configKey + ":" + COUNT;
				m_lineCharts.put(key, createLineChart(config.getTitle() + Chinese.Suffix_COUNT));
			}
			if (showAvg(config)) {
				String key = configKey + ":" + AVG;
				m_lineCharts.put(key, createLineChart(config.getTitle() + Chinese.Suffix_AVG));
			}
		}
	}

	private LineChart createLineChart(String title) {
		LineChart lineChart = new LineChart();
		lineChart.setTitle(title);
		lineChart.setStart(m_start);
		lineChart.setSize(m_pointNumber);
		lineChart.setStep(TimeUtil.ONE_MINUTE * m_interval);
		return lineChart;
	}

	public void generateBaselineChart() {
		for (String key : m_lineCharts.keySet()) {
			LineChart lineChart = m_lineCharts.get(key);
			Date yesterday = TaskHelper.todayZero(m_start);
			boolean isAvg = key.toUpperCase().endsWith(AVG);
			int index = (int) ((m_start.getTime() + 8 * TimeUtil.ONE_HOUR) % TimeUtil.ONE_DAY / TimeUtil.ONE_MINUTE);
			double[] yesterdayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, yesterday);
			Date today = TaskHelper.tomorrowZero(m_start);
			double[] todayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, today);
			double[] value = new double[m_realPointNumber];
			double[] day = yesterdayBaseline;
			for (int i = 0; i < m_realPointNumber; i++) {
				int j = (index + i * m_interval) % (HOUR * MINUTE);
				if (j == 0 && index != 0) {
					day = todayBaseline;
				}
				if (day == null) {
					continue;
				}
				if (isAvg) {
					value[i] = avgOfArray(day, i * m_interval);
				} else {
					value[i] = sumOfArray(day, i * m_interval);
				}
			}
			lineChart.addSubTitle("Baseline");
			lineChart.addValue(value);
		}
	}

	public void generateLineCharts() {
		Map<String, Map<String, double[][]>> metricDatas = m_displayMerger.getMetricStatistic();

		for (Entry<String, Map<String, double[][]>> entry : metricDatas.entrySet()) {
			String key = entry.getKey();
			Map<String, double[][]> value = entry.getValue();
			LineChart lineChart = m_lineCharts.get(key);
			List<double[]> resultValues = new ArrayList<double[]>();
			List<String> subTitles = new ArrayList<String>();
			boolean isAvg = key.toUpperCase().endsWith(AVG);

			if (lineChart == null) {
				lineChart = createLineChart(key);
				m_lineCharts.put(key, lineChart);
			}

			lineChart.setSubTitles(subTitles);
			lineChart.setValues(resultValues);

			for (Entry<String, double[][]> metricItem : value.entrySet()) {

				String subTitle = metricItem.getKey();
				double[][] metricItemData = metricItem.getValue();
				double[] resultValue = new double[m_realPointNumber];

				subTitles.add(subTitle);
				resultValues.add(resultValue);

				for (int hour = 0; hour < metricItemData.length; hour++) {
					for (int i = 0; i < MINUTE / m_interval; i++) {
						int index = hour * MINUTE / m_interval + i;
						if (metricItemData[hour] == null) {
							continue;
						}
						if (index >= m_realPointNumber) {
							break;
						}
						if (isAvg) {
							resultValue[index] = avgOfArray(metricItemData[hour], i * m_interval);
						} else {
							resultValue[index] = sumOfArray(metricItemData[hour], i * m_interval);
						}
					}
				}
			}

		}
	}

	public Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> getAbtests() {
		return m_abtests;
	}

	public List<LineChart> getLineCharts() {
		return new ArrayList<LineChart>(m_lineCharts.values());
	}

	public MetricDisplay setBaselineService(BaselineService baselineService) {
		m_baselineService = baselineService;
		return this;
	}

	private double avgOfArray(double[] values, int j) {
		double result = 0;
		for (int i = j; i < j + m_interval; i++) {
			if (values[i] >= 0) {
				result += values[i];
			}
		}
		return result / m_interval;
	}

	private double sumOfArray(double[] values, int j) {
		double result = 0;
		for (int i = j; i < j + m_interval; i++) {
			if (values[i] >= 0) {
				result += values[i];
			}
		}
		return result;
	}

}
