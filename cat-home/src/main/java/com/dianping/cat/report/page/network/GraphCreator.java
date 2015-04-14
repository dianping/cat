package com.dianping.cat.report.page.network;

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
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;

public class GraphCreator extends AbstractGraphCreator {

	public Map<String, LineChart> buildChartData(String productLine, final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures) {
		Map<String, List<String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet());
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<AlertMetric> alertKeys = m_alertInfo.queryLastestAlarmKey(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, List<String>> keyMapEntry : aggregationKeys.entrySet()) {
			String chartTitle = keyMapEntry.getKey();
			LineChart lineChart = new LineChart();
			lineChart.setTitle(chartTitle);
			lineChart.setHtmlTitle(chartTitle);
			lineChart.setId(chartTitle);
			lineChart.setStart(startDate);
			lineChart.setStep(step * TimeHelper.ONE_MINUTE);
			lineChart.setUnit(buildUnit(chartTitle));

			for (String key : keyMapEntry.getValue()) {
				if (dataWithOutFutures.containsKey(key)) {
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

					buildLineChartTitle(productLine, lineChart, key, alertKeys);
					addLastMinuteData(current, all, m_lastMinute, endDate);
					convertFlowMetric(lineChart, current, buildLineTitle(key));
				} else {
					lineChart.add(chartTitle, buildNoneData(startDate, endDate, 1));
				}
			}
			charts.put(chartTitle, lineChart);
		}
		return charts;
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		return buildChartData(productLine, oldCurrentValues, startDate, endDate, dataWithOutFutures);
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

	private void buildLineChartTitle(String productLine, LineChart lineChart, String key, List<AlertMetric> alertKeys) {
		String title = lineChart.getHtmlTitle();
		String realKey = key.substring(0, key.lastIndexOf(":"));

		// alertKeys格式 = [domain:Metric:key]
		if (containsAlert(productLine, realKey, alertKeys) && !title.startsWith("<span style='color:red'>")) {
			lineChart.setHtmlTitle("<span style='color:red'>" + title + "</span>");
		} else {
			lineChart.setHtmlTitle(title);
		}
	}

	private String buildLineTitle(String lineKey) {
		return lineKey.substring(lineKey.lastIndexOf("-") + 1, lineKey.lastIndexOf(":"));
	}

	private boolean containsAlert(String productLine, String key, List<AlertMetric> metrics) {
		for (AlertMetric metric : metrics) {
			if (metric.getGroup().equals(productLine) && metric.getMetricId().equals(key)) {
				return true;
			}
		}

		return false;
	}

	private boolean isSumTypeMetric(String group) {
		if (isFlowMetric(group) || group.toLowerCase().endsWith("-discard/error")) {
			return true;
		} else {
			return false;
		}
	}

	private Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
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
		return currentValues;
	}
}
