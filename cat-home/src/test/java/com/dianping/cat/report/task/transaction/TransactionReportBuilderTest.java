package com.dianping.cat.report.task.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class TransactionReportBuilderTest  extends ComponentTestCase {
	
	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildDailyTask(TransactionReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-26"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWeeklyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildWeeklyTask(TransactionReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-13"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMonthlyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildMonthlyTask(TransactionReportBuilder.ID, Constants.CAT,
			      new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
