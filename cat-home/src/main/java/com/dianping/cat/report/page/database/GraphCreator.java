package com.dianping.cat.report.page.database;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;

public class GraphCreator extends AbstractGraphCreator {

	private Map<String, LineChart> buildChartData(String productLine, final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<AlertMetric> alertKeys = m_alertInfo.queryLastestAlarmKey(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			buildLineChartTitle(alertKeys, lineChart, key);
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setUnit("Value/秒");
			lineChart.setMinYlable(lineChart.queryMinYlable(value));
			lineChart.setStep(step * TimeHelper.ONE_MINUTE);
			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(Chinese.CURRENT_VALUE, current);
			charts.put(key, lineChart);
		}
		return charts;
	}

	public Map<String, LineChart> buildChartsByProductLine(String group, String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(group, productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		return buildChartData(productLine, oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	private Map<String, double[]> buildGraphData(MetricReport metricReport) {
		Map<String, double[]> datas = m_pruductDataFetcher.buildGraphData(metricReport);
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();

		for (Entry<String, double[]> entry : datas.entrySet()) {
			String key = entry.getKey();

			if (key.endsWith(MetricType.SUM.name())) {
				putKey(datas, values, key);
			}
		}
		return values;
	}

	private void buildLineChartTitle(List<AlertMetric> alertKeys, LineChart chart, String key) {
		int index = key.lastIndexOf(":");
		String type = key.substring(index + 1);
		String des = queryMetricItemDes(type);
		String[] strs = key.split(":");
		String title = strs[2] + des;

		chart.setTitle(title);
		chart.setHtmlTitle(title);
		chart.setId(key);
	}

	private Map<String, double[]> prepareAllData(String group, String productLine, Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			Map<String, double[]> currentValues = queryMetricValueByDate(group, productLine, start);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	private String queryMetricItemDes(String type) {
		String des = "";

		if (MetricType.AVG.name().equals(type)) {
			des = Chinese.Suffix_AVG;
		} else if (MetricType.SUM.name().equals(type)) {
			des = Chinese.Suffix_SUM;
		} else if (MetricType.COUNT.name().equals(type)) {
			des = Chinese.Suffix_COUNT;
		}
		return des;
	}

	private Map<String, double[]> queryMetricValueByDate(String group, String productLine, long start) {
		MetricReport metricReport = m_metricReportService.queryMetricReport(productLine, new Date(start));
		List<String> keys = DatabaseGroup.KEY_GROUPS.get(group);
		DatabaseReportFilter filter = new DatabaseReportFilter(keys);

		filter.visitMetricReport(metricReport);
		metricReport = filter.getReport();

		Map<String, double[]> currentValues = buildGraphData(metricReport);

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
