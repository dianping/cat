package com.dianping.cat.report.task.utilization;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.report.task.utilization.UtilizationReportBuilder;

public class UtilizationBuilderTest extends ComponentTestCase{
	
	@Test
	public void testHourlyReport() throws Exception{
		Cat.initialize("192.168.7.43");
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildHourlyTask("utilization", "cat", new SimpleDateFormat("yyyyMMddHH").parse("2013082508"));
	}
	
	@Test
	public void testDailyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildDailyTask("utilization", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130825"));
	}

	@Test
	public void testWeeklyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildWeeklyTask("utilization", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130717"));
	}
	
	@Test
	public void testMonthlyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildMonthlyTask("utilization", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

	
	
}
