package com.dianping.cat.report.page.cdn.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;

public class CdnGraphCreator extends AbstractGraphCreator {

	private final static String CDN = "cdn";

	private Map<String, LineChart> buildInfoChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();

		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			lineChart.setId(key);
			lineChart.setTitle(key);
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeHelper.ONE_MINUTE);

			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(entry.getKey(), current);
			charts.put(key, lineChart);
		}
		return charts;
	}

	private Map<String, double[]> fetchData(MetricReport report) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();
		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();

			if (!data.containsKey(key)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(key, values);
			}
			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(key)[id] += segment.getSum();
			}
		}

		return data;
	}

	public Map<String, double[]> prepareAllData(Date startDate, Date endDate, String cdn, String province, String city) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("metricType", Constants.METRIC_CDN);
		properties.put("cdn", cdn);
		properties.put("province", province);
		properties.put("city", city);

		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			MetricReport report = m_metricReportService.queryCdnReport(CDN, properties, new Date(start));
			Map<String, double[]> currentValues;
			currentValues = fetchData(report);

			mergeMap(sourceValue, currentValues, totalSize, index);
			index++;
		}

		return sourceValue;
	}

	public Map<String, LineChart> queryBaseInfo(Date startDate, Date endDate, String cdn, String province, String city) {
		Map<String, double[]> oldCurrentValues = prepareAllData(startDate, endDate, cdn, province, city);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
		Map<String, LineChart> lineCharts = buildInfoChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);

		return lineCharts;
	}
}
