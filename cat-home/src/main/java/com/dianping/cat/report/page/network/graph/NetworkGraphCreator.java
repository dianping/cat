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
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
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
			if (chartTitle.endsWith("-flow")) {
				lineChart.setUnit("流量(MB/分钟)");
			} else {
				lineChart.setUnit("value");
			}

			for (String key : keyMapEntry.getValue()) {
				if (dataWithOutFutures.containsKey(key)) {
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);
					Map<Long, Double> convertedData = new LinkedHashMap<Long, Double>();

					addLastMinuteData(current, all, m_lastMinute, endDate);
					
					if (chartTitle.endsWith("-flow")) {
						for (Entry<Long, Double> currentEntry : current.entrySet()) {
							double result = currentEntry.getValue() / 1000.0;

							convertedData.put(currentEntry.getKey(), result);
						}
						lineChart.add(buildLineTitle(key), convertedData);
					} else {
						lineChart.add(buildLineTitle(key), current);
					}
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

	private Map<String, double[]> buildGraphData(MetricReport metricReport) {
		Map<String, double[]> datas = m_pruductDataFetcher.buildGraphData(metricReport);
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();

		for (Entry<String, MetricItem> metricItem : metricReport.getMetricItems().entrySet()) {
			String key = metricItem.getKey();
			String type = metricItem.getValue().getType();

			if ("S,C".equalsIgnoreCase(type)) {
				String avgKey = key + ":" + MetricType.SUM.name();
				putKey(datas, values, avgKey);
			} else if ("S".equalsIgnoreCase(type)) {
				String avgKey = key + ":" + MetricType.COUNT.name();
				putKey(datas, values, avgKey);
			} else if ("T".equalsIgnoreCase(type)) {
				String avgKey = key + ":" + MetricType.AVG.name();
				putKey(datas, values, avgKey);
			}
		}
		return values;
	}

	private Map<String, double[]> queryMetricValueByDate(String productLine, long start) {
		MetricReport metricReport = m_metricReportService.queryMetricReport(productLine, new Date(start));
		Map<String, double[]> currentValues = buildGraphData(metricReport);
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
			return buildGraphData(lastMetricReport);
		}
		return currentValues;
	}

	public String buildLineTitle(String lineKey) {
		int colonIndex = lineKey.lastIndexOf(":");
		String tmp = lineKey.substring(0, colonIndex > -1 ? colonIndex : 0);

		return tmp.substring(tmp.lastIndexOf("-") + 1);
	}

	private Map<String, List<String>> buildLineChartKeys(Set<String> keys) {
		Set<String> groupSet = new LinkedHashSet<String>();
		Map<String, List<String>> aggregationKeys = new LinkedHashMap<String, List<String>>();

		// key = domain:Metric:groupName-lineKey:SUM
		for (String key : keys) {
			try {
				int colonIndex = key.lastIndexOf(":");
				String tmp = key.substring(0, colonIndex); // domain:Metric:groupName-lineKey
				int hyphenIndex = tmp.lastIndexOf("-");
				String groupName = tmp.substring(0, hyphenIndex); // domain:Metric:groupName

				groupSet.add(groupName);
			} catch (Exception exception) {
				Cat.logError(new RuntimeException("network agent send metric [" + key + "]  error"));
			}
		}

		for (String group : groupSet) {
			List<String> keyList = new ArrayList<String>();
			for (String key : keys) {
				if (key.startsWith(group)) {
					keyList.add(key);
				}
			}
			String groupName = group.substring(group.lastIndexOf(":") + 1); // groupName

			aggregationKeys.put(groupName, keyList); // [groupName:[domain:Metric:groupName-lineKey:SUM]...]
		}

		return aggregationKeys;
	}

}
