package com.dianping.cat.report.page.metric.chart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricGroup.entity.MetricKeyConfig;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.system.config.MetricGroupConfigManager;

public class GraphCreator implements LogEnabled {

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private DataExtractor m_dataExtractor;

	@Inject
	private MetricDataFetcher m_pruductDataFetcher;

	@Inject
	private CachedMetricReportService m_metricReportService;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricGroupConfigManager m_metricGroupConfigManager;

	private int m_lastMinute = 6;

	private int m_extraTime = 1;

	private Logger m_logger;

	private void addLastMinuteData(Map<Long, Double> current, Map<Long, Double> all, int minute, Date end) {
		long endTime = 0;
		long currentTime = System.currentTimeMillis();
		if (end.getTime() > currentTime) {
			endTime = currentTime - currentTime % TimeUtil.ONE_MINUTE - m_extraTime * TimeUtil.ONE_MINUTE;
		} else {
			endTime = end.getTime();
		}
		long start = endTime - minute * TimeUtil.ONE_MINUTE;
		Set<Long> sets = new HashSet<Long>();

		for (Entry<Long, Double> entry : current.entrySet()) {
			if (entry.getKey() >= start) {
				sets.add(entry.getKey());
			}
		}
		for (Long temp : sets) {
			current.remove(temp);
		}

		for (int i = minute; i > 0; i--) {
			long time = endTime - i * TimeUtil.ONE_MINUTE;
			Double value = all.get(time);

			if (value != null) {
				current.put(time, value);
			}
		}
	}

	private Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		int step = m_dataExtractor.getStep();
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			lineChart.setTitle(findTitle(key));
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);
			double[] baselines = queryBaseline(key, startDate, endDate);

			// lineChart.add(Chinese.CURRENT_VALUE, allCurrentValues.get(key));
			// lineChart.add(Chinese.BASELINE_VALUE, m_dataExtractor.extract(baselines));
			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(Chinese.CURRENT_VALUE, current);
			lineChart.add(Chinese.BASELINE_VALUE, convertToMap(m_dataExtractor.extract(baselines), startDate, step));
			charts.put(key, lineChart);
		}
		return charts;
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	public Map<String, LineChart> buildDashboard(Date start, Date end) {
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (showInDashboard(productLine.getId())) {
				allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end));
			}
		}
		List<MetricItemConfig> configs = new ArrayList<MetricItemConfig>(m_metricConfigManager.getMetricConfig()
		      .getMetricItemConfigs().values());

		Collections.sort(configs, new Comparator<MetricItemConfig>() {
			@Override
			public int compare(MetricItemConfig o1, MetricItemConfig o2) {
				return (int) (o1.getShowDashboardOrder() * 100 - o2.getShowDashboardOrder() * 100);
			}
		});

		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
		for (MetricItemConfig config : configs) {
			String key = config.getId();
			if (config.getShowAvg() && config.getShowAvgDashboard()) {
				String avgKey = key + ":" + MetricType.AVG.name();
				put(allCharts, result, avgKey);
			}
			if (config.getShowCount() && config.getShowCountDashboard()) {
				String countKey = key + ":" + MetricType.COUNT.name();
				put(allCharts, result, countKey);
			}
			if (config.getShowSum() && config.getShowSumDashboard()) {
				String sumKey = key + ":" + MetricType.SUM.name();
				put(allCharts, result, sumKey);
			}
		}
		return result;
	}

	public Map<String, LineChart> buildDashboardByGroup(Date start, Date end, String metricGroup) {
		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
		List<MetricKeyConfig> metricConfigs = m_metricGroupConfigManager.queryMetricGroupConfig(metricGroup);
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (isProductLineInGroup(productLine.getId(), metricConfigs)) {
				allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end));
			}
		}
		for (MetricKeyConfig metric : metricConfigs) {
			String domain = metric.getMetricDomain();
			String type = metric.getMetricType().equalsIgnoreCase("metric") ? "Metric" : metric.getMetricType();
			String key = metric.getMetricKey();
			String id = m_metricConfigManager.buildMetricKey(domain, type, key) + ":"
			      + metric.getDisplayType().toUpperCase();

			put(allCharts, result, id);
		}
		return result;
	}

	public double[] convert(double[] value, int removeLength) {
		int length = value.length;
		int newLength = length - removeLength;
		double[] result = new double[newLength];

		for (int i = 0; i < newLength; i++) {
			result[i] = value[i];
		}
		return result;
	}

	private Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();

		for (int i = 0; i < length; i++) {
			map.put(startTime + step * i * TimeUtil.ONE_MINUTE, data[i]);
		}
		return map;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private String findTitle(String key) {
		int index = key.lastIndexOf(":");
		String id = key.substring(0, index);
		String type = key.substring(index + 1);
		MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(id);
		String des = "";

		if (MetricType.AVG.name().equals(type)) {
			des = Chinese.Suffix_AVG;
		} else if (MetricType.SUM.name().equals(type)) {
			des = Chinese.Suffix_SUM;
		} else if (MetricType.COUNT.name().equals(type)) {
			des = Chinese.Suffix_COUNT;
		}
		return config.getTitle() + des;
	}

	private boolean isCurrentMode(Date date) {
		Date current = TimeUtil.getCurrentHour();

		return current.getTime() == date.getTime() - TimeUtil.ONE_HOUR;
	}

	public boolean isProductLineInGroup(String productLine, List<MetricKeyConfig> configs) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine);
		List<MetricItemConfig> metricConfig = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(domains));

		for (MetricKeyConfig metric : configs) {
			String domain = metric.getMetricDomain();
			String type = metric.getMetricType();
			String key = metric.getMetricKey();

			for (MetricItemConfig item : metricConfig) {
				if (item.getDomain().equalsIgnoreCase(domain) && item.getType().equalsIgnoreCase(type)
				      && item.getMetricKey().equalsIgnoreCase(key)) {
					return true;
				}
			}
		}
		return false;
	}

	private void mergeMap(Map<String, double[]> all, Map<String, double[]> item, int size, int index) {
		for (Entry<String, double[]> entry : item.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			double[] result = all.get(key);

			if (result == null) {
				result = new double[size];
				all.put(key, result);
			}
			if (value != null) {
				int length = value.length;
				int pos = index * 60;
				for (int i = 0; i < length && pos < size; i++, pos++) {
					result[pos] = value[i];
				}
			}
		}
	}

	private Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
		long start = startDate.getTime();
		long end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new HashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine);
			List<MetricItemConfig> metricConfigs = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(
			      domains));
			Map<String, double[]> currentValues = queryMetricValueByDate(productLine, start, metricConfigs);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	private void put(Map<String, LineChart> charts, Map<String, LineChart> result, String key) {
		LineChart value = charts.get(key);

		if (value != null) {
			result.put(key, charts.get(key));
		}
	}

	private double[] queryBaseline(String key, Date start, Date end) {
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_MINUTE);
		double[] result = new double[size];
		int index = 0;
		long startLong = start.getTime();
		long endLong = end.getTime();

		for (; startLong < endLong; startLong += TimeUtil.ONE_HOUR) {
			double[] values = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, key, new Date(startLong));

			for (int j = 0; j < values.length; j++) {
				result[index * 60 + j] = values[j];
			}
			index++;
		}
		return result;
	}

	private Map<String, double[]> queryMetricValueByDate(String productLine, long start,
	      List<MetricItemConfig> metricConfigs) {
		MetricReport metricReport = m_metricReportService.query(productLine, new Date(start));
		Map<String, double[]> currentValues = m_pruductDataFetcher.buildGraphData(metricReport, metricConfigs);
		double sum = 0;

		for (Entry<String, double[]> entry : currentValues.entrySet()) {
			double[] value = entry.getValue();
			int length = value.length;

			for (int i = 0; i < length; i++) {
				sum = sum + value[i];
			}
		}

		// if current report is not exist, use last day value replace it.
		if (sum <= 0 && start < TimeUtil.getCurrentHour().getTime()) {
			MetricReport lastMetricReport = m_metricReportService.query(productLine, new Date(start - TimeUtil.ONE_DAY));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:ss");

			m_logger.error("Metric report is not exsit, productLine:" + productLine + " ,date:"
			      + sdf.format(new Date(start)));
			return m_pruductDataFetcher.buildGraphData(lastMetricReport, metricConfigs);
		}
		return currentValues;
	}

	private Map<String, double[]> removeFutureData(Date endDate, final Map<String, double[]> allCurrentValues) {
		if (isCurrentMode(endDate)) {
			// remove the minute of future
			Map<String, double[]> newCurrentValues = new HashMap<String, double[]>();
			int step = m_dataExtractor.getStep();
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			int removeLength = 60 / step - (minute / step);

			for (Entry<String, double[]> entry : allCurrentValues.entrySet()) {
				String key = entry.getKey();
				double[] value = entry.getValue();

				newCurrentValues.put(key, convert(value, removeLength));
			}
			return newCurrentValues;
		}
		return allCurrentValues;
	}

	private boolean showInDashboard(String productline) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productline);
		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(domains));

		for (MetricItemConfig config : configs) {
			if (config.isShowAvgDashboard() || config.isShowCountDashboard() || config.isShowSumDashboard()) {
				return true;
			}
		}
		return false;
	}

}
