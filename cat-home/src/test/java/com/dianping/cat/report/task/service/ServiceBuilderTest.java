package com.dianping.cat.report.task.service;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.DomainManager;

public class ServiceBuilderTest  extends ComponentTestCase{
	
	@Test
	public void testHourlyReport() throws Exception{
		Cat.initialize("192.168.7.43");
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildHourlyTask("service", "cat", new SimpleDateFormat("yyyyMMddHH").parse("2013071210"));
	}
	
	@Test
	public void testDailyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildDailyTask("service", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130712"));
	}

	@Test
	public void testWeeklyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildWeeklyTask("service", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130710"));
	}
	
	@Test
	public void testMonthlyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildMonthlyTask("service", "cat", new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

}
