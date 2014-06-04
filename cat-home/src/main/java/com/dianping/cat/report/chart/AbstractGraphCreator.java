package com.dianping.cat.report.chart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.alert.AlertInfo;
import com.dianping.cat.system.config.MetricGroupConfigManager;

public abstract class AbstractGraphCreator implements LogEnabled {
	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected DataExtractor m_dataExtractor;

	@Inject
	protected MetricDataFetcher m_pruductDataFetcher;

	@Inject
	protected CachedMetricReportService m_metricReportService;

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected MetricGroupConfigManager m_metricGroupConfigManager;

	@Inject
	protected AlertInfo m_alertInfo;

	protected int m_lastMinute = 6;

	protected int m_extraTime = 1;

	protected Logger m_logger;

	protected void addLastMinuteData(Map<Long, Double> current, Map<Long, Double> all, int minute, Date end) {
		int step = m_dataExtractor.getStep();

		if (step == 1) {
			return;
		}
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

	protected abstract Map<String, double[]> buildGraphData(MetricReport metricReport,
	      List<MetricItemConfig> metricConfigs);

	protected double[] convert(double[] value, int removeLength) {
		int length = value.length;
		int newLength = length - removeLength;
		double[] result = new double[newLength];

		for (int i = 0; i < newLength; i++) {
			result[i] = value[i];
		}
		return result;
	}

	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
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

	private boolean isCurrentMode(Date date) {
		Date current = TimeUtil.getCurrentHour();

		return current.getTime() == date.getTime() - TimeUtil.ONE_HOUR;
	}

	protected void mergeMap(Map<String, double[]> all, Map<String, double[]> item, int size, int index) {
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

	protected Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			Map<String, double[]> currentValues = queryMetricValueByDate(productLine, start);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	protected void put(Map<String, LineChart> charts, Map<String, LineChart> result, String key) {
		LineChart value = charts.get(key);

		if (value != null) {
			result.put(key, charts.get(key));
		}
	}

	protected void putKey(Map<String, double[]> datas, Map<String, double[]> values, String key) {
		double[] value = datas.get(key);

		if (value == null) {
			value = new double[60];
		}
		values.put(key, value);
	}

	protected double[] queryBaseline(String key, Date start, Date end) {
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

	private Map<String, double[]> queryMetricValueByDate(String productLine, long start) {
		MetricReport metricReport = m_metricReportService.queryMetricReport(productLine, new Date(start));
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine);
		List<MetricItemConfig> metricConfigs = m_metricConfigManager.queryMetricItemConfigs(domains);

		Collections.sort(metricConfigs, new Comparator<MetricItemConfig>() {
			@Override
			public int compare(MetricItemConfig o1, MetricItemConfig o2) {
				return (int) (o1.getViewOrder() * 100 - o2.getViewOrder() * 100);
			}
		});
		Map<String, double[]> currentValues = buildGraphData(metricReport, metricConfigs);
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
			MetricReport lastMetricReport = m_metricReportService.queryMetricReport(productLine, new Date(start
			      - TimeUtil.ONE_DAY));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:ss");

			m_logger.error("Replace error value, Metric report is not exsit, productLine:" + productLine + " ,date:"
			      + sdf.format(new Date(start)));
			return buildGraphData(lastMetricReport, metricConfigs);
		}
		return currentValues;
	}

	protected Map<String, double[]> removeFutureData(Date endDate, final Map<String, double[]> allCurrentValues) {
		if (isCurrentMode(endDate)) {
			// remove the minute of future
			Map<String, double[]> newCurrentValues = new LinkedHashMap<String, double[]>();
			int step = m_dataExtractor.getStep();

			if (step <= 0) {
				return allCurrentValues;
			}
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
}
