package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.report.service.DefaultReportServiceManager;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.service.impl.AlertReportService;
import com.dianping.cat.report.service.impl.BugReportService;
import com.dianping.cat.report.service.impl.CrossReportService;
import com.dianping.cat.report.service.impl.DependencyReportService;
import com.dianping.cat.report.service.impl.EventReportService;
import com.dianping.cat.report.service.impl.HeartbeatReportService;
import com.dianping.cat.report.service.impl.HeavyReportService;
import com.dianping.cat.report.service.impl.MatrixReportService;
import com.dianping.cat.report.service.impl.MetricReportService;
import com.dianping.cat.report.service.impl.NetTopologyReportService;
import com.dianping.cat.report.service.impl.ProblemReportService;
import com.dianping.cat.report.service.impl.ServiceReportService;
import com.dianping.cat.report.service.impl.StateReportService;
import com.dianping.cat.report.service.impl.TopReportService;
import com.dianping.cat.report.service.impl.TransactionReportService;
import com.dianping.cat.report.service.impl.UtilizationReportService;

public class ReportServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ReportService.class, TransactionAnalyzer.ID, TransactionReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, EventAnalyzer.ID, EventReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, ProblemAnalyzer.ID, ProblemReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, MatrixAnalyzer.ID, MatrixReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, CrossAnalyzer.ID, CrossReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, StateAnalyzer.ID, StateReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(ReportService.class, Constants.REPORT_BUG, BugReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, Constants.REPORT_UTILIZATION, UtilizationReportService.class).req(
		      HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class,
		      HourlyReportContentDao.class, DailyReportContentDao.class, WeeklyReportContentDao.class,
		      MonthlyReportContentDao.class));
		all.add(C(ReportService.class, Constants.REPORT_SERVICE, ServiceReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, Constants.REPORT_HEAVY, HeavyReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(ReportService.class, Constants.REPORT_NET_TOPOLOGY, NetTopologyReportService.class).req(
		      HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class,
		      HourlyReportContentDao.class, DailyReportContentDao.class, WeeklyReportContentDao.class,
		      MonthlyReportContentDao.class));
		all.add(C(ReportService.class, Constants.REPORT_ALERT, AlertReportService.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(ReportService.class, TopAnalyzer.ID, TopReportService.class).req(HourlyReportDao.class,
		      HourlyReportContentDao.class));
		all.add(C(ReportService.class, DependencyAnalyzer.ID, DependencyReportService.class).req(HourlyReportDao.class,
		      HourlyReportContentDao.class));
		all.add(C(ReportService.class, HeartbeatAnalyzer.ID, HeartbeatReportService.class).req(HourlyReportDao.class,
		      HourlyReportContentDao.class));
		all.add(C(ReportService.class, MetricAnalyzer.ID, MetricReportService.class).req(HourlyReportDao.class,
		      BusinessReportDao.class));

		all.add(C(ReportServiceManager.class, DefaultReportServiceManager.class).req(HourlyReportDao.class,
		      DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class).req(HourlyReportContentDao.class,
		      DailyReportContentDao.class, WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		return all;
	}
}
