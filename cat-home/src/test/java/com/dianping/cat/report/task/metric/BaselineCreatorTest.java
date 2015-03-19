package com.dianping.cat.report.task.metric;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.page.metric.task.MetricBaselineReportBuilder;

public class BaselineCreatorTest extends ComponentTestCase {

	@Test
	public void testCreateData() {
		MetricBaselineReportBuilder builder = lookup(MetricBaselineReportBuilder.class);
		Date date = TimeHelper.getCurrentMonth();
		long start = date.getTime();

		for (; start < System.currentTimeMillis(); start = start + TimeHelper.ONE_DAY) {

			builder.buildDailyTask("Metric", "", new Date(start));
		}
	}

	@Test
	public void test() {
		DefaultBaselineCreator creator = new DefaultBaselineCreator();
		double[] data = new double[60];

		for (int i = 0; i < data.length; i++) {
			data[i] = i;

			if (i == 20 || i == 30 || i == 40) {
				data[i] = 100;
			}
			if (i == 21 || i == 31 || i == 41) {
				data[i] = 200;
			}
			if (i == 25 || i == 35 || i == 45) {
				data[i] = 300;
			}
			if (i == 26 || i == 36 || i == 46) {
				data[i] = 300;
			}
		}

		double[] result = creator.denoise(data, 10);

		for (int i = 0; i < result.length; i++) {
			Assert.assertEquals(result[i] < 60, true);
		}
	}
}
