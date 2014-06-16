package com.dianping.cat.report.page.cdn.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;

public class CdnGraphCreator extends AbstractGraphCreator {
	@Inject
	private CdnConfig m_cdnConfig;

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
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);

			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(entry.getKey(), current);
			charts.put(key, lineChart);
		}
		return charts;
	}

	private Map<String, double[]> fetchAllData(MetricReport report) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		for (String cdn : m_cdnConfig.getAllCdnNames()) {
			double[] values = new double[60];
			for (int i = 0; i < 60; i++)
				values[i] = 0;
			data.put(cdn, values);
		}

		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();
			String temp[] = key.split(":");
			String cdn = temp[0];

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(cdn)[id] += segment.getSum();
			}
		}

		return data;
	}

	private Map<String, double[]> fetchCdnData(MetricReport report, String cdn) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		Map<String, MetricItem> items = report.getMetricItems();
		String keyCdn, keyProvince;

		for (Entry<String, MetricItem> item : items.entrySet()) {
			try {
				String key = item.getKey();
				String temp[] = key.split(":");
				keyCdn = temp[0];
				keyProvince = temp[1];
				
				if (!keyCdn.equals(cdn)) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}

			if (!data.containsKey(keyProvince)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(keyProvince, values);
			}

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(keyProvince)[id] += segment.getSum();
			}
		}

		return data;
	}

	private Map<String, double[]> fetchCityData(MetricReport report, String cdn, String province, String city) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		Map<String, MetricItem> items = report.getMetricItems();
		String keyCdn, keyProvince, keyCity, sip;

		for (Entry<String, MetricItem> item : items.entrySet()) {
			try {
				String key = item.getKey();
				String temp[] = key.split(":");
				keyCdn = temp[0];
				keyProvince = temp[1];
				keyCity = temp[2];
				sip = temp[3];

				if (!keyCdn.equals(cdn) || !keyProvince.equals(province) || !keyCity.equals(city))
					continue;
			} catch (Exception e) {
				continue;
			}

			if (!data.containsKey(sip)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(sip, values);
			}

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(sip)[id] += segment.getSum();
			}
		}

		return data;
	}

	private Map<String, double[]> fetchProvinceData(MetricReport report, String cdn, String province) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		Map<String, MetricItem> items = report.getMetricItems();
		String keyCdn, keyProvince, keyCity;

		for (Entry<String, MetricItem> item : items.entrySet()) {

			try {
				String key = item.getKey();
				String temp[] = key.split(":");
				keyCdn = temp[0];
				keyProvince = temp[1];
				keyCity = temp[2];

				if (!keyCdn.equals(cdn) || !keyProvince.equals(province))
					continue;
			} catch (Exception e) {
				continue;
			}

			if (!data.containsKey(keyCity)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(keyCity, values);
			}

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(keyCity)[id] += segment.getSum();
			}
		}

		return data;
	}

	public Map<String, double[]> prepareAllData(Date startDate, Date endDate, String cdn, String province, String city) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("metricType", Constants.METRIC_CDN);
		properties.put("cdn", cdn);
		properties.put("province", province);
		properties.put("city", city);

		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			MetricReport report = m_metricReportService.queryCdnReport(m_cdnConfig.GROUP, properties, new Date(start));
			Map<String, double[]> currentValues;

			if (province.equals("未知")) {
				city = "未知";
			}

			if (cdn.equals("ALL")) {
				currentValues = fetchAllData(report);
			} else if (province.equals("ALL")) {
				currentValues = fetchCdnData(report, cdn);
			} else if (city.equals("ALL")) {
				currentValues = fetchProvinceData(report, cdn, province);
			} else {
				currentValues = fetchCityData(report, cdn, province, city);
			}
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
