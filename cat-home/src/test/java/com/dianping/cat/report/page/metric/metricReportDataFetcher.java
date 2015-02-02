package com.dianping.cat.report.page.metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.graph.metric.MetricDataFetcher;

public class metricReportDataFetcher extends ComponentTestCase {

	private DataExtractor m_dataExtractor;

	protected MetricDataFetcher m_pruductDataFetcher;

	protected CachedMetricReportService m_metricReportService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void setUp() throws Exception {
		super.setUp();

		try {
			m_dataExtractor = lookup(DataExtractor.class);
			m_pruductDataFetcher = lookup(MetricDataFetcher.class);
			m_metricReportService = lookup(CachedMetricReportService.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void buildReport() {
		String date = "20140827";
		List<String> productLines = Arrays.asList("SLB");
		Date start = null;
		Date end = null;
		try {
			start = m_sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		end = new Date(start.getTime() + TimeHelper.ONE_DAY);

		for (String productLine : productLines) {
			Map<String, double[]> oldCurrentValues = prepareAllData(productLine, start, end);
			Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);

			for (Entry<String, double[]> entry : allCurrentValues.entrySet()) {
				String key = entry.getKey();
				double[] value = entry.getValue();

				System.out.println(key);

				for (int i = 0; i < value.length; i++) {
					String hour = convertTime(i / 6);
					String minute = convertTime(i % 6 * 10);
					String time = hour + ":" + minute;

					System.out.println(time + ", " + value[i]);
				}
			}
		}
	}

	public Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
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

	private String convertTime(int n) {
		String time = "";

		if (n < 10) {
			time = "0" + n;
		} else {
			time = String.valueOf(n);
		}
		return time;
	}
}
