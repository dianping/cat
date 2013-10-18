package com.dianping.cat.report.page.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class MetricDisplay extends BaseVisitor {

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Map<Integer, com.dianping.cat.home.dal.abtest.Abtest> m_abtests = new HashMap<Integer, com.dianping.cat.home.dal.abtest.Abtest>();

	private BaselineService m_baselineService;

	private DataExtractor m_dataExtractor;

	private Date m_start;

	private boolean m_isDashboard;

	private int m_timeRange;

	private int m_interval = 1;

	private String m_abtest;

	private String m_product;

	private static final int MIN_POINT_NUMBER = 60;

	private static final int MAX_POINT_NUMBER = 180;

	private static final String SUM = MetricType.SUM.name();

	private static final String COUNT = MetricType.COUNT.name();

	private static final String AVG = MetricType.AVG.name();

	private static final String METRIC_STRING = "metric";

	private ReportService m_reportService;

	private ModelService<MetricReport> m_service;

	private final Map<String, MetricReport> m_metricReportMap = new LinkedHashMap<String, MetricReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, MetricReport> eldest) {
			return size() > 1000;
		}
	};

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

	private MetricReport getReportFromDB(String product, long date) {
		String key = product + date;
		MetricReport result = m_metricReportMap.get(key);
		if (result == null) {
			Date start = new Date(date);
			Date end = new Date(date + TimeUtil.ONE_HOUR);
			try {
				result = m_reportService.queryMetricReport(product, start, end);
				m_metricReportMap.put(key, result);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	private MetricReport getReport(ModelPeriod period, String product, long date) {
		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, date);
			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();
				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			return getReportFromDB(product, date);
		}
	}

	public MetricDisplay(String product, String abtest, Date start, boolean isDashboard, int timeRange) {
		m_product = product;
		m_isDashboard = isDashboard;
		m_start = start;
		m_timeRange = timeRange;
		m_abtest = abtest;
		m_interval = intervalCalculate(m_timeRange);
		m_dataExtractor = new DataExtractor(m_timeRange, m_interval);
	}

	private int intervalCalculate(int timeRange) {
		int[] values = { 1, 2, 3, 6, 10, 20, 30, 60 };
		for (int value : values) {
			int pm = timeRange * 60 / value;
			if (pm >= MIN_POINT_NUMBER && pm < MAX_POINT_NUMBER) {
				return value;
			}
		}
		int pm = timeRange * 60 / 60;
		if (pm > MAX_POINT_NUMBER) {
			return 60;
		} else {
			return 1;
		}
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
		int pointNumber = m_timeRange * 60 / m_interval;

		lineChart.setTitle(title);
		lineChart.setStart(m_start);
		lineChart.setSize(pointNumber);
		lineChart.setStep(TimeUtil.ONE_MINUTE * m_interval);
		return lineChart;
	}

	public void generateAllCharts() {
		long startTime = m_start.getTime();
		for (MetricDate metricDate : MetricDate.values()) {
			MetricReportMerger merger = new MetricReportMerger(m_abtest, metricDate.getTitle());
			long time = startTime + metricDate.getIndex() * TimeUtil.ONE_DAY;
			for (int index = 0; index < m_timeRange; index++) {
				ModelPeriod period = ModelPeriod.getByTime(time);
				MetricReport report = getReport(period, m_product, time);

				if (report != null) {
					merger.visitMetricReport(index, report);
				}
				time = time + TimeUtil.ONE_HOUR;
			}
			generateLineCharts(merger);
			if (metricDate.equals(MetricDate.CURRENT) && m_abtest.equals("-1")) {
				generateBaselineChart(merger);
			}
		}

	}

	public void generateBaselineChart(MetricReportMerger merger) {
		long time = m_start.getTime();

		for (int index = 0; index < m_timeRange; index++) {
			ModelPeriod period = ModelPeriod.getByTime(time);
			MetricReport report = getReport(period, m_product, time);

			if (report != null) {
				merger.visitMetricReport(index, report);
			}
			time = time + TimeUtil.ONE_HOUR;
		}

		for (String key : m_lineCharts.keySet()) {
			LineChart lineChart = m_lineCharts.get(key);

			Date yesterday = TaskHelper.todayZero(m_start);
			int offset = (int) ((m_start.getTime() + 8 * TimeUtil.ONE_HOUR) % TimeUtil.ONE_DAY / TimeUtil.ONE_MINUTE);
			double[] yesterdayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, yesterday);
			Date today = TaskHelper.tomorrowZero(m_start);
			double[] todayBaseline = m_baselineService.queryDailyBaseline(METRIC_STRING, key, today);
			List<double[]> datas = new ArrayList<double[]>();
			if (yesterdayBaseline != null) {
				datas.add(yesterdayBaseline);
			} else if (todayBaseline != null) {
				datas.add(todayBaseline);
			}

			double[] value = m_dataExtractor.extract(datas, offset);
			lineChart.addSubTitle("Baseline");
			lineChart.addValue(value);
		}
	}

	public void generateLineCharts(MetricReportMerger merger) {
		Map<String, Map<String, double[][]>> metricDatas = merger.getMetricStatistic();

		for (Entry<String, Map<String, double[][]>> entry : metricDatas.entrySet()) {
			String key = entry.getKey();
			Map<String, double[][]> value = entry.getValue();
			LineChart lineChart = m_lineCharts.get(key);

			if (lineChart == null) {
				lineChart = createLineChart(key);
				m_lineCharts.put(key, lineChart);
			}

			for (Entry<String, double[][]> metricItem : value.entrySet()) {

				String subTitle = metricItem.getKey();
				double[][] metricItemData = metricItem.getValue();
				List<double[]> datas = new ArrayList<double[]>();
				for (double[] data : metricItemData) {
					if (data == null) {
						data = new double[60];
					}
					datas.add(data);
				}

				double[] resultValue = m_dataExtractor.extract(datas, 0);
				lineChart.addSubTitle(subTitle);
				lineChart.addValue(resultValue);
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

	public MetricDisplay setReportService(ReportService m_reportService) {
		this.m_reportService = m_reportService;
		return this;
	}

	public MetricDisplay setService(ModelService<MetricReport> m_service) {
		this.m_service = m_service;
		return this;
	}

}
