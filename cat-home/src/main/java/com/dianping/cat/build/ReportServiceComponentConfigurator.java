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
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.MonthlyReportContentDao;
import com.dianping.cat.core.dal.WeeklyReportContentDao;
import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.network.service.NetTopologyReportService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.statistics.service.BugReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.SystemReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.system.page.router.service.RouterConfigService;

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

		all.add(C(AppReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		return all;
	}
}
