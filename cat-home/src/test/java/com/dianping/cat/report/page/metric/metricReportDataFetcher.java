package com.dianping.cat.report.page.metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.DataExtractor;
import com.dianping.cat.report.page.metric.graph.MetricGraphCreator;

public class metricReportDataFetcher extends ComponentTestCase {

	private MetricGraphCreator m_graphCreator;

	private DataExtractor m_dataExtractor;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void setUp() throws Exception {
		super.setUp();

		try {
			m_graphCreator = lookup(MetricGraphCreator.class);
			m_dataExtractor = lookup(DataExtractor.class);

			Assert.assertNotNull(m_graphCreator);
			Assert.assertNotNull(m_dataExtractor);
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
		end = new Date(start.getTime() + TimeUtil.ONE_DAY);

		for (String productLine : productLines) {
			Map<String, double[]> oldCurrentValues = m_graphCreator.prepareAllData(productLine, start, end);
			Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
			Map<String, double[]> dataWithOutFutures = m_graphCreator.removeFutureData(end, allCurrentValues);

			for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
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
