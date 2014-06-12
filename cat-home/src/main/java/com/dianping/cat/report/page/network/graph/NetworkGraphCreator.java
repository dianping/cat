package com.dianping.cat.report.page.network.graph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.alert.MetricType;

public class NetworkGraphCreator extends AbstractGraphCreator {

	public Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, List<String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet());
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		int step = m_dataExtractor.getStep();

		for (Entry<String, List<String>> keyMapEntry : aggregationKeys.entrySet()) {
			String chartTitle = keyMapEntry.getKey();
			LineChart lineChart = new LineChart();
			lineChart.setTitle(chartTitle);
			lineChart.setHtmlTitle(chartTitle);
			lineChart.setId(chartTitle);
			lineChart.setStart(startDate);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);
			lineChart.setUnit(buildUnit(keyMapEntry.getKey()));

			for (String key : keyMapEntry.getValue()) {
				if (dataWithOutFutures.containsKey(key)) {
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

					addLastMinuteData(current, all, m_lastMinute, endDate);
					convertLineChartData(lineChart, current, key);
				}
			}
			charts.put(chartTitle, lineChart);
		}
		return charts;
	}

	private void convertLineChartData(LineChart lineChart, Map<Long, Double> current, String key) {
		
		if (isFlowMetric(lineChart.getId())) {
			Map<Long, Double> convertedData = new LinkedHashMap<Long, Double>();

			for (Entry<Long, Double> currentEntry : current.entrySet()) {
				double result = currentEntry.getValue() / 1000.0;

				convertedData.put(currentEntry.getKey(), result);
			}
			lineChart.add(buildLineTitle(key), convertedData);
		} else {
			lineChart.add(buildLineTitle(key), current);
		}
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	private Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
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

	private Map<String, double[]> queryMetricValueByDate(String productLine, long start) {
		MetricReport metricReport = m_metricReportService.queryMetricReport(productLine, new Date(start));
		Map<String, double[]> currentValues = m_pruductDataFetcher.buildGraphData(metricReport);
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
			return m_pruductDataFetcher.buildGraphData(lastMetricReport);
		}
		return currentValues;
	}

	private String buildLineTitle(String lineKey) {

		return lineKey.substring(lineKey.lastIndexOf("-") + 1, lineKey.lastIndexOf(":"));
	}

	private Set<String> buildKeysWithoutType(Set<String> keys) {
		Set<String> result = new LinkedHashSet<String>();

		for (String key : keys) {
			int index = key.lastIndexOf(":");
			String frontKey = key.substring(0, index);

			result.add(frontKey);// domain:Metric:groupName-lineKey
		}
		return result;
	}

	private Map<String, List<String>> buildLineChartKeys(Set<String> originKeys) {
		Set<String> groupSet = new LinkedHashSet<String>();
		Map<String, List<String>> aggregationKeys = new LinkedHashMap<String, List<String>>();
		Set<String> keys = buildKeysWithoutType(originKeys);

		// key = domain:Metric:groupName-lineKey
		for (String key : keys) {
			try {
				int hyphenIndex = key.lastIndexOf("-");
				String groupName = key.substring(0, hyphenIndex); // domain:Metric:groupName

				groupSet.add(groupName);
			} catch (Exception exception) {
				Cat.logError(new RuntimeException("network agent send metric [" + key + "]  error"));
			}
		}

		for (String group : groupSet) {
			List<String> keyList = new ArrayList<String>();
			for (String key : keys) {
				if (key.startsWith(group)) {
					if (isSumTypeMetric(group)) {
						keyList.add(key + ":" + MetricType.SUM);
					} else {
						keyList.add(key + ":" + MetricType.AVG);
					}
				}
			}
			String groupName = group.substring(group.lastIndexOf(":") + 1); // groupName

			aggregationKeys.put(groupName, keyList); // [groupName:[domain:Metric:groupName-lineKey:SUM]...]
		}
		return aggregationKeys;
	}

	private boolean isFlowMetric(String title) {
		if (title.endsWith("-flow")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isSumTypeMetric(String group) {
		if (isFlowMetric(group) || group.toLowerCase().endsWith("-discard/error")) {
			return true;
		} else {
			return false;
		}
	}

	private String buildUnit(String chartTitle) {
		if (isFlowMetric(chartTitle)) {
			return "流量(MB/分钟)";
		} else {
			return "value/分钟";
		}
	}
}
