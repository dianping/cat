package com.dianping.cat.report.page.cdn.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;

public class CdnGraphCreator extends AbstractGraphCreator {
	private static final String GROUP = "system-cdn";

	private static final Map<String, String> CDNS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("10.1.1.1", "帝联");
			put("10.1.1.2", "网宿");
		}
	};

	@Inject
	private IpService m_ipService;

	private Map<String, LineChart> buildInfoChartData(
			final Map<String, double[]> datas, Date startDate, Date endDate,
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
			Map<Long, Double> current = convertToMap(
					dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(entry.getKey(), current);
			charts.put(key, lineChart);
		}
		return charts;
	}

	private Map<String, double[]> fetchAllData(MetricReport report) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		for (Entry<String, String> cdn : CDNS.entrySet()) {
			double[] values = new double[60];
			for (int i = 0; i < 60; i++)
				values[i] = 0;
			data.put(cdn.getValue(), values);
		}

		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();
			String vip = key.split("_")[1];
			if (!CDNS.containsKey(vip))
				continue;
			String cdn = CDNS.get(vip);

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
		String province, sip;

		for (Entry<String, MetricItem> item : items.entrySet()) {
			try {
				String key = item.getKey();
				if (CDNS.get(key.split("_")[1]).compareTo(cdn) != 0)
					continue;
				sip = key.split("_")[2];
			} catch (Exception e) {
				continue;
			}

			IpInfo ipInfo = m_ipService.findIpInfoByString(sip);

			if (ipInfo == null) {
				province = "其他";
			} else {
				province = ipInfo.getProvince();
			}
			if (!data.containsKey(province)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(province, values);
			}

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(province)[id] += segment.getSum();
			}
		}

		return data;
	}

	private Map<String, double[]> fetchCityData(MetricReport report,
			String cdn, String province, String city) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		Map<String, MetricItem> items = report.getMetricItems();
		String sip;

		for (Entry<String, MetricItem> item : items.entrySet()) {
			try {
				String key = item.getKey();
				if (CDNS.get(key.split("_")[1]).compareTo(cdn) != 0)
					continue;
				sip = key.split("_")[2];
			} catch (Exception e) {
				continue;
			}

			IpInfo ipInfo = m_ipService.findIpInfoByString(sip);
			if (ipInfo == null || ipInfo.getProvince().compareTo(province) != 0
					|| ipInfo.getCity().compareTo(city) != 0)
				continue;

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

	private Map<String, double[]> fetchProvinceData(MetricReport report,
			String cdn, String province) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		Map<String, MetricItem> items = report.getMetricItems();
		String city, sip;

		for (Entry<String, MetricItem> item : items.entrySet()) {

			try {
				String key = item.getKey();
				if (CDNS.get(key.split("_")[1]).compareTo(cdn) != 0)
					continue;
				sip = key.split("_")[2];
			} catch (Exception e) {
				continue;
			}

			IpInfo ipInfo = m_ipService.findIpInfoByString(sip);
			if (ipInfo == null || ipInfo.getProvince().compareTo(province) != 0)
				continue;

			city = ipInfo.getCity();
			if (!data.containsKey(city)) {
				double[] values = new double[60];
				for (int i = 0; i < 60; i++)
					values[i] = 0;
				data.put(city, values);
			}

			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				data.get(city)[id] += segment.getSum();
			}
		}

		return data;
	}

	public Map<String, double[]> prepareAllData(Date startDate, Date endDate,
			String cdn, String province, String city) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);

		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			MetricReport report = m_metricReportService.queryMetricReport(
					GROUP, new Date(start));
			Map<String, double[]> currentValues;
			
			if (cdn.compareTo("ALL") == 0) {
				currentValues = fetchAllData(report);
			} else if (province.compareTo("ALL") == 0) {
				currentValues = fetchCdnData(report, cdn);
			} else if (city.compareTo("ALL") == 0) {
				currentValues = fetchProvinceData(report, cdn, province);
			} else {
				currentValues = fetchCityData(report, cdn, province, city);
			}
			mergeMap(sourceValue, currentValues, totalSize, index);
			index++;
		}

		return sourceValue;
	}

	public Map<String, LineChart> queryBaseInfo(Date startDate, Date endDate,
			String cdn, String province, String city) {
		Map<String, double[]> oldCurrentValues = prepareAllData(startDate,
				endDate, cdn, province, city);
		Map<String, double[]> allCurrentValues = m_dataExtractor
				.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate,
				allCurrentValues);

		Map<String, LineChart> lineCharts = buildInfoChartData(
				oldCurrentValues, startDate, endDate, dataWithOutFutures);
		return lineCharts;
	}
}
