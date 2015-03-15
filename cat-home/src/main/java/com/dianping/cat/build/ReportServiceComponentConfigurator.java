package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.dal.BusinessReportDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.report.service.impl.BugReportService;
import com.dianping.cat.report.service.impl.CrossReportService;
import com.dianping.cat.report.service.impl.DependencyReportService;
import com.dianping.cat.report.service.impl.EventReportService;
import com.dianping.cat.report.service.impl.HeartbeatReportService;
import com.dianping.cat.report.service.impl.HeavyReportService;
import com.dianping.cat.report.service.impl.HighloadReportService;
import com.dianping.cat.report.service.impl.JarReportService;
import com.dianping.cat.report.service.impl.MatrixReportService;
import com.dianping.cat.report.service.impl.MetricReportService;
import com.dianping.cat.report.service.impl.NetTopologyReportService;
import com.dianping.cat.report.service.impl.ProblemReportService;
import com.dianping.cat.report.service.impl.RouterConfigService;
import com.dianping.cat.report.service.impl.ServiceReportService;
import com.dianping.cat.report.service.impl.StateReportService;
import com.dianping.cat.report.service.impl.StorageReportService;
import com.dianping.cat.report.service.impl.SystemReportService;
import com.dianping.cat.report.service.impl.TopReportService;
import com.dianping.cat.report.service.impl.TransactionReportService;
import com.dianping.cat.report.service.impl.UtilizationReportService;

public class ReportServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TransactionReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(EventReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ProblemReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(MatrixReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(CrossReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(StateReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(StorageReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(BugReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(UtilizationReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ServiceReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(HeavyReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(NetTopologyReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(RouterConfigService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(HighloadReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(JarReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(SystemReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(TopReportService.class).req(HourlyReportDao.class, HourlyReportContentDao.class));
		all.add(C(DependencyReportService.class).req(HourlyReportDao.class, HourlyReportContentDao.class));
		all.add(C(HeartbeatReportService.class).req(HourlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportDao.class, DailyReportContentDao.class));
		all.add(C(MetricReportService.class).req(HourlyReportDao.class, BusinessReportDao.class));

		return all;
	}
}
