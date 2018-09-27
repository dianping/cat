package com.dianping.cat.report.task.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class EventReportBuilderTest extends ComponentTestCase {
	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, EventReportBuilder.ID);

		try {
			builder.buildDailyTask(EventReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-21"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWeeklyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, EventReportBuilder.ID);

		try {
			builder.buildWeeklyTask(EventReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-13"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMonthlyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, EventReportBuilder.ID);

		try {
			builder.buildMonthlyTask(EventReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
