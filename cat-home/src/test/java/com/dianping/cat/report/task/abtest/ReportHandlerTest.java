package com.dianping.cat.report.task.abtest;

import java.util.Calendar;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.system.page.abtest.ReportHandler;

public class ReportHandlerTest extends ComponentTestCase {

	@Test
	public void printJson() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -2);
		ReportHandler handler = lookup(ReportHandler.class);

		AbtestReport query = new AbtestReport();
		query.setRunId(152);
		query.setEndTime(calendar.getTime());

		calendar.add(Calendar.HOUR, -24);
		query.setStartTime(calendar.getTime());

		String goal = "order";
		String period = "hour";

		AbtestReport report = handler.buildReport(query, goal, period);
		
		System.out.println(report);

	}

}
