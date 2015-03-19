package com.dianping.cat.report.task.router;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class RouterReportBuilderTest  extends ComponentTestCase{

	@Test
	public void test() throws ParseException{
		RouterConfigBuilder builder = (RouterConfigBuilder)lookup(TaskBuilder.class,RouterConfigBuilder.ID);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-01-26 00:00:00");
		builder.buildDailyTask(Constants.REPORT_JAR, "cat", period);
	}
}
