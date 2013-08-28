package com.dianping.cat.report.task.service;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.DomainManager;
import com.dianping.cat.service.Constants;

public class ServiceBuilderTest  extends ComponentTestCase{
	
	@Test
	public void testHourlyReport() throws Exception{
		Cat.initialize("192.168.7.43");
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildHourlyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMddHH").parse("2013082011"));
	}
	
	@Test
	public void testDailyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildDailyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130712"));
	}

	@Test
	public void testWeeklyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildWeeklyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130710"));
	}
	
	@Test
	public void testMonthlyReport() throws Exception{
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		DomainManager manager = lookup(DomainManager.class);
		
		manager.initialize();
		builder.buildMonthlyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

}
