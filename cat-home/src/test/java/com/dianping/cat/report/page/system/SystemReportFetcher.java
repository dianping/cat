package com.dianping.cat.report.page.system;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.page.system.graph.SystemGraphCreator;

public class SystemReportFetcher extends ComponentTestCase {

	private SystemGraphCreator m_graphCreator;

	private DataExtractor m_dataExtractor;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void setUp() throws Exception {
		super.setUp();

		try {
			m_graphCreator = lookup(SystemGraphCreator.class);
			m_dataExtractor = lookup(DataExtractor.class);

			Assert.assertNotNull(m_graphCreator);
			Assert.assertNotNull(m_dataExtractor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void buildReport() {

		String date = "20140821";
		List<String> groupList = Arrays.asList("account-mq");

		for (String group : groupList) {
			group = "system-" + group;
			Map<String, String> pars = new HashMap<String, String>();

			pars.put("ip", "all");
			pars.put("type", "system");
			pars.put("metricType", Constants.METRIC_SYSTEM_MONITOR);
			Date start = null;
			Date end = null;
			try {
				start = m_sdf.parse(date);
				end = new Date(start.getTime() + TimeHelper.ONE_DAY);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Map<String, double[]> oldCurrentValues = m_graphCreator.prepareAllData(group, pars, new HashSet<String>(),
			      start, end);
			Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
			Map<String, double[]> dataWithOutFutures = m_graphCreator.removeFutureData(end, allCurrentValues);

			for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
				String key = entry.getKey();
				double[] value = entry.getValue();

				if (key.startsWith("cpuUsage") && key.endsWith("AVG")) {
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
