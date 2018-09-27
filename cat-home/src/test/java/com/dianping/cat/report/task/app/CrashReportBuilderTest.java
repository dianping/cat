package com.dianping.cat.report.task.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.crash.task.CrashReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class CrashReportBuilderTest extends ComponentTestCase {

	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, CrashReportBuilder.ID);

		try {
			((CrashReportBuilder) builder).runDailyTask(CrashReportBuilder.ID, "点评主APP",
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-26"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
