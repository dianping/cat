package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;

public class DependencyHourlyBuilder extends ComponentTestCase{
	

	@Test
	public void test() throws Exception{
		DependencyReportBuilder builder = (DependencyReportBuilder)lookup(DependencyReportBuilder.class);
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-06-13 10:00");
		
		builder.buildHourlyTask(DependencyAnalyzer.ID, "Cat", date);
	}
}
