package com.dianping.cat.report.task;

import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.task.metric.MetricBaselineReportBuilder;

public class BaseLineCreatorTest extends ComponentTestCase {

	@Test
	public void test() {
		MetricBaselineReportBuilder builder = lookup(MetricBaselineReportBuilder.class);
		Date date = TimeUtil.getCurrentMonth();
		long start = date.getTime();

		for (; start < System.currentTimeMillis(); start = start + TimeUtil.ONE_DAY) {

			builder.buildDailyTask("Metric", "", new Date(start));
		}
	}
}
