package com.dianping.cat.report.task.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.business.task.BusinessBaselineReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class BusinessBaseLineCreatorTest extends ComponentTestCase {

	@Test
	public void testCreateData() throws ParseException {
		BusinessBaselineReportBuilder builder = (BusinessBaselineReportBuilder) lookup(TaskBuilder.class, BusinessBaselineReportBuilder.ID);
		
		builder.buildDailyTask(BusinessBaselineReportBuilder.ID, "group",
		      new SimpleDateFormat("yyyy-MM-dd").parse("2016-03-10"));
	}

}
