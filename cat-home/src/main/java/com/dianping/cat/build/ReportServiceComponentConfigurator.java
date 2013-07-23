package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.report.service.HourlyReportService;
import com.dianping.cat.report.service.MonthlyReportCache;
import com.dianping.cat.report.service.MonthlyReportService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.WeeklyReportCache;
import com.dianping.cat.report.service.WeeklyReportService;
import com.dianping.cat.report.service.impl.DailyReportServiceImpl;
import com.dianping.cat.report.service.impl.HourlyReportServiceImpl;
import com.dianping.cat.report.service.impl.MonthlyReportServiceImpl;
import com.dianping.cat.report.service.impl.ReportServiceImpl;
import com.dianping.cat.report.service.impl.WeeklyReportServiceImpl;

public class ReportServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		
		all.add(C(HourlyReportService.class, HourlyReportServiceImpl.class) //
		      .req(HourlyReportDao.class, BusinessReportDao.class));

		all.add(C(DailyReportService.class, DailyReportServiceImpl.class)//
		      .req(DailyReportDao.class));

		all.add(C(WeeklyReportService.class, WeeklyReportServiceImpl.class)//
		      .req(WeeklyReportDao.class));

		all.add(C(MonthlyReportService.class, MonthlyReportServiceImpl.class)//
		      .req(MonthlyReportDao.class));

		all.add(C(WeeklyReportCache.class)//
		      .req(DailyReportService.class, HourlyReportService.class, ServerConfigManager.class));

		all.add(C(MonthlyReportCache.class)//
		      .req(DailyReportService.class, HourlyReportService.class, ServerConfigManager.class));

		all.add(C(ReportService.class, ReportServiceImpl.class)//
		      .req(HourlyReportService.class, DailyReportService.class, WeeklyReportService.class,
		            MonthlyReportService.class)//
		      .req(WeeklyReportCache.class, MonthlyReportCache.class));


		return all;
	}
}
