package com.dianping.cat.report.task.metric;

import java.text.DecimalFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.baseline.impl.DefaultBaselineCreator;

public class BaselineCreatorTest extends ComponentTestCase {

	@Test
	public void testCreateData() {
		MetricBaselineReportBuilder builder = lookup(MetricBaselineReportBuilder.class);
		Date date = TimeUtil.getCurrentMonth();
		long start = date.getTime();

		for (; start < System.currentTimeMillis(); start = start + TimeUtil.ONE_DAY) {

			builder.buildDailyTask("Metric", "", new Date(start));
		}
	}

	@Test
	public void test() {
		DefaultBaselineCreator creator = new DefaultBaselineCreator();
		double[] data = new double[24 * 60];

		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}

		double[] result = creator.denoise(data, 5);
		DecimalFormat df = new DecimalFormat("#.#");

		for (int i = 0; i < result.length; i++) {
			System.out.print(df.format(result[i]) + " ");
			if (i % 60 == 59) {
				System.out.println();
			}
		}
	}
}
