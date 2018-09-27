package com.dianping.cat.report.task.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class AppReportBuilderTest extends ComponentTestCase {

	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, AppReportBuilder.ID);

		try {
			((AppReportBuilder) builder).runDailyTask(AppReportBuilder.ID, "点评主APP",
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-05-20"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
