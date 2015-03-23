package com.dianping.cat.report.task.utilization;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.service.HostinfoService;

public class UtilizationBuilderTest extends ComponentTestCase{
	
	@Test
	public void testHourlyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);
		
		hostinfoService.initialize();
		builder.buildHourlyTask(Constants.REPORT_UTILIZATION,Constants.CAT, new SimpleDateFormat("yyyyMMddHH").parse("2013082617"));
	}
	
	@Test
	public void testDailyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);
		
		hostinfoService.initialize();
		builder.buildDailyTask(Constants.REPORT_UTILIZATION, Constants.CAT, new SimpleDateFormat("yyyyMMdd").parse("20130826"));
	}

	@Test
	public void testWeeklyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);
		
		hostinfoService.initialize();	
		builder.buildWeeklyTask(Constants.REPORT_UTILIZATION, Constants.CAT, new SimpleDateFormat("yyyyMMdd").parse("20130717"));
	}
	
	@Test
	public void testMonthlyReport() throws Exception{
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);
		
		hostinfoService.initialize();
		builder.buildMonthlyTask(Constants.REPORT_UTILIZATION, Constants.CAT, new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

	
	
}
