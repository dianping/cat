package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.WeeklyreportDao;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.report.service.HourlyReportService;
import com.dianping.cat.report.service.MonthReportCache;
import com.dianping.cat.report.service.MonthReportService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.WeeklyReportCache;
import com.dianping.cat.report.service.WeeklyReportService;
import com.dianping.cat.report.service.impl.DailyReportServiceImpl;
import com.dianping.cat.report.service.impl.HourlyReportServiceImpl;
import com.dianping.cat.report.service.impl.MonthReportServiceImpl;
import com.dianping.cat.report.service.impl.ReportServiceImpl;
import com.dianping.cat.report.service.impl.WeeklyReportServiceImpl;

public class ReportServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		
		all.add(C(HourlyReportService.class, HourlyReportServiceImpl.class) //
		      .req(ReportDao.class, BusinessReportDao.class));

		all.add(C(DailyReportService.class, DailyReportServiceImpl.class)//
		      .req(DailyreportDao.class));

		all.add(C(WeeklyReportService.class, WeeklyReportServiceImpl.class)//
		      .req(WeeklyreportDao.class));

		all.add(C(MonthReportService.class, MonthReportServiceImpl.class)//
		      .req(MonthreportDao.class));

		all.add(C(WeeklyReportCache.class)//
		      .req(DailyReportService.class, HourlyReportService.class, ServerConfigManager.class));

		all.add(C(MonthReportCache.class)//
		      .req(DailyReportService.class, HourlyReportService.class, ServerConfigManager.class));

		all.add(C(ReportService.class, ReportServiceImpl.class)//
		      .req(HourlyReportService.class, DailyReportService.class, WeeklyReportService.class,
		            MonthReportService.class)//
		      .req(WeeklyReportCache.class, MonthReportCache.class));


		return all;
	}
}
