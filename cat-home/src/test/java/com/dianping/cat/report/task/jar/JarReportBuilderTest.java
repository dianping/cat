package com.dianping.cat.report.task.jar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class JarReportBuilderTest  extends ComponentTestCase{

	@Test
	public void test() throws ParseException{
		JarReportBuilder builder = (JarReportBuilder)lookup(TaskBuilder.class,JarReportBuilder.ID);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-11-14 13:00:00");
		builder.buildHourlyTask(Constants.REPORT_JAR, "cat", period);
	}
}
