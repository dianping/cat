package com.dianping.cat.report.task.problem;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class ProblemReportBuilderTest extends ComponentTestCase {
	
	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, ProblemReportBuilder.ID);

		try {
			builder.buildDailyTask(ProblemReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-25"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWeeklyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, ProblemReportBuilder.ID);

		try {
			builder.buildWeeklyTask(ProblemReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-20"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMonthlyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, ProblemReportBuilder.ID);

		try {
			builder.buildMonthlyTask(ProblemReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
