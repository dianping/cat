package com.dianping.cat.report.task.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class AppReportBuilderTest extends ComponentTestCase {

	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, AppReportBuilder.ID);

		try {
			builder.buildDailyTask(AppReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2015-03-24"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
