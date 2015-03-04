package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.report.alert.exception.AlertReportBuilder;
import com.dianping.cat.report.alert.sender.sender.SenderManager;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.page.storage.StorageMergeHelper;
import com.dianping.cat.report.page.transaction.TransactionMergeHelper;
import com.dianping.cat.report.service.BaselineService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.service.app.AppDataService;
import com.dianping.cat.report.service.impl.DefaultBaselineService;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.bug.BugReportBuilder;
import com.dianping.cat.report.task.cached.CachedReportBuilder;
import com.dianping.cat.report.task.cached.CachedReportTask;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.database.AppDatabasePruner;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;
import com.dianping.cat.report.task.event.EventGraphCreator;
import com.dianping.cat.report.task.event.EventMerger;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.task.highload.HighLoadReportBuilder;
import com.dianping.cat.report.task.jar.JarReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.metric.BaselineConfigManager;
import com.dianping.cat.report.task.metric.BaselineCreator;
import com.dianping.cat.report.task.metric.MetricBaselineReportBuilder;
import com.dianping.cat.report.task.metric.MetricPointParser;
import com.dianping.cat.report.task.metric.impl.DefaultBaselineCreator;
import com.dianping.cat.report.task.network.NetTopologyReportBuilder;
import com.dianping.cat.report.task.notify.AppDataComparisonNotifier;
import com.dianping.cat.report.task.notify.NotifyTaskBuilder;
import com.dianping.cat.report.task.notify.ReportRender;
import com.dianping.cat.report.task.notify.ReportRenderImpl;
import com.dianping.cat.report.task.notify.render.AppDataComparisonRender;
import com.dianping.cat.report.task.overload.CapacityUpdateStatusManager;
import com.dianping.cat.report.task.overload.CapacityUpdateTask;
import com.dianping.cat.report.task.overload.CapacityUpdater;
import com.dianping.cat.report.task.overload.DailyCapacityUpdater;
import com.dianping.cat.report.task.overload.HourlyCapacityUpdater;
import com.dianping.cat.report.task.overload.MonthlyCapacityUpdater;
import com.dianping.cat.report.task.overload.TableCapacityService;
import com.dianping.cat.report.task.overload.WeeklyCapacityUpdater;
import com.dianping.cat.report.task.problem.ProblemGraphCreator;
import com.dianping.cat.report.task.problem.ProblemMerger;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.router.RouterConfigBuilder;
import com.dianping.cat.report.task.service.ServiceReportBuilder;
import com.dianping.cat.report.task.state.StateReportBuilder;
import com.dianping.cat.report.task.storage.StorageReportBuilder;
import com.dianping.cat.report.task.system.SystemReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionGraphCreator;
import com.dianping.cat.report.task.transaction.TransactionMerger;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.dianping.cat.report.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.ExceptionRuleConfigManager;
import com.dianping.cat.system.config.NetGraphConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;

public class TaskComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultTaskConsumer.class) //
		      .req(TaskDao.class, ReportFacade.class));

		all.add(C(TransactionGraphCreator.class));
		all.add(C(EventGraphCreator.class));
		all.add(C(ProblemGraphCreator.class));

		all.add(C(TransactionMerger.class));
		all.add(C(EventMerger.class));
		all.add(C(ProblemMerger.class));

		all.add(C(MetricPointParser.class));
		all.add(C(BaselineCreator.class, DefaultBaselineCreator.class));
		all.add(C(BaselineService.class, DefaultBaselineService.class).req(BaselineDao.class));
		all.add(C(BaselineConfigManager.class, BaselineConfigManager.class));

		all.add(C(TaskBuilder.class, MetricBaselineReportBuilder.ID, MetricBaselineReportBuilder.class)
		      .req(ReportServiceManager.class, MetricPointParser.class)//
		      .req(MetricConfigManager.class, ProductLineConfigManager.class)//
		      .req(BaselineCreator.class, BaselineService.class, BaselineConfigManager.class));

		all.add(C(TaskBuilder.class, TransactionReportBuilder.ID, TransactionReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportServiceManager.class)//
		      .req(TransactionGraphCreator.class, TransactionMerger.class));

		all.add(C(TaskBuilder.class, EventReportBuilder.ID, EventReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportServiceManager.class)//
		      .req(EventGraphCreator.class, EventMerger.class));//

		all.add(C(TaskBuilder.class, ProblemReportBuilder.ID, ProblemReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportServiceManager.class)//
		      .req(ProblemGraphCreator.class, ProblemMerger.class));

		all.add(C(TaskBuilder.class, HeartbeatReportBuilder.ID, HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, ReportServiceManager.class));

		all.add(C(TaskBuilder.class, BugReportBuilder.ID, BugReportBuilder.class).req(ReportServiceManager.class,
		      ServerConfigManager.class));

		all.add(C(TaskBuilder.class, ServiceReportBuilder.ID, ServiceReportBuilder.class).req(ReportServiceManager.class,
		      ServerConfigManager.class));

		all.add(C(TaskBuilder.class, MatrixReportBuilder.ID, MatrixReportBuilder.class).req(ReportServiceManager.class));

		all.add(C(TaskBuilder.class, CrossReportBuilder.ID, CrossReportBuilder.class).req(ReportServiceManager.class));

		all.add(C(TaskBuilder.class, StateReportBuilder.ID, StateReportBuilder.class) //
		      .req(ServerConfigManager.class, HostinfoService.class, ProjectService.class) //
		      .req(ReportServiceManager.class));

		all.add(C(TaskBuilder.class, RouterConfigBuilder.ID, RouterConfigBuilder.class).req(ReportServiceManager.class,
		      RouterConfigManager.class));

		all.add(C(TaskBuilder.class, AlertReportBuilder.ID, AlertReportBuilder.class).req(ReportServiceManager.class,
		      ExceptionRuleConfigManager.class, ServerConfigManager.class));

		all.add(C(TaskBuilder.class, HeavyReportBuilder.ID, HeavyReportBuilder.class).req(ReportServiceManager.class,
		      ServerConfigManager.class));

		all.add(C(TaskBuilder.class, UtilizationReportBuilder.ID, UtilizationReportBuilder.class).req(
		      ReportServiceManager.class, TransactionMergeHelper.class, ServerConfigManager.class));

		all.add(C(TaskBuilder.class, DependencyReportBuilder.ID, DependencyReportBuilder.class).req(
		      ReportServiceManager.class, TopologyGraphBuilder.class, TopologyGraphDao.class));

		all.add(C(TaskBuilder.class, NetTopologyReportBuilder.ID, NetTopologyReportBuilder.class).req(
		      ReportServiceManager.class, NetGraphBuilder.class, NetGraphConfigManager.class));

		all.add(C(TaskBuilder.class, JarReportBuilder.ID, JarReportBuilder.class).req(ReportServiceManager.class,
		      ServerConfigManager.class));

		all.add(C(TaskBuilder.class, SystemReportBuilder.ID, SystemReportBuilder.class).req(ReportServiceManager.class,
		      ProductLineConfigManager.class, CachedMetricReportService.class));

		all.add(C(TaskBuilder.class, CachedReportBuilder.ID, CachedReportBuilder.class).req(CachedReportTask.class));

		all.add(C(TaskBuilder.class, StorageReportBuilder.ID, StorageReportBuilder.class).req(ReportServiceManager.class,
		      StorageMergeHelper.class));

		all.add(C(TaskBuilder.class, CmdbInfoReloadBuilder.ID, CmdbInfoReloadBuilder.class).req(ProjectUpdateTask.class));

		all.add(C(CapacityUpdateStatusManager.class).req(OverloadDao.class, ConfigDao.class));

		all.add(C(CapacityUpdater.class, HourlyCapacityUpdater.ID, HourlyCapacityUpdater.class).req(OverloadDao.class,
		      HourlyReportContentDao.class, HourlyReportDao.class, CapacityUpdateStatusManager.class));

		all.add(C(CapacityUpdater.class, DailyCapacityUpdater.ID, DailyCapacityUpdater.class).req(OverloadDao.class,
		      DailyReportContentDao.class, DailyReportDao.class, CapacityUpdateStatusManager.class));

		all.add(C(CapacityUpdater.class, WeeklyCapacityUpdater.ID, WeeklyCapacityUpdater.class).req(OverloadDao.class,
		      WeeklyReportContentDao.class, WeeklyReportDao.class, CapacityUpdateStatusManager.class));

		all.add(C(CapacityUpdater.class, MonthlyCapacityUpdater.ID, MonthlyCapacityUpdater.class).req(OverloadDao.class,
		      MonthlyReportContentDao.class, MonthlyReportDao.class, CapacityUpdateStatusManager.class));

		all.add(C(TableCapacityService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, OverloadDao.class));

		all.add(C(TaskBuilder.class, CapacityUpdateTask.ID, CapacityUpdateTask.class)
		      .req(CapacityUpdater.class, HourlyCapacityUpdater.ID, "m_hourlyUpdater")
		      .req(CapacityUpdater.class, DailyCapacityUpdater.ID, "m_dailyUpdater")
		      .req(CapacityUpdater.class, WeeklyCapacityUpdater.ID, "m_weeklyUpdater")
		      .req(CapacityUpdater.class, MonthlyCapacityUpdater.ID, "m_monthlyUpdater"));

		all.add(C(TaskBuilder.class, HighLoadReportBuilder.ID, HighLoadReportBuilder.class)//
		      .req(ReportServiceManager.class, ProjectService.class));

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(AppDataComparisonRender.class));

		all.add(C(AppDataComparisonNotifier.class).req(AppDataService.class)
		      .req(AppComparisonConfigManager.class, AppConfigManager.class)
		      .req(SenderManager.class, AppDataComparisonRender.class));

		all.add(C(AppDatabaseConfigurator.class).req(AppCommandDataDao.class, AppSpeedDataDao.class));

		all.add(C(TaskBuilder.class, NotifyTaskBuilder.ID, NotifyTaskBuilder.class)
		      .req(ReportRender.class, SenderManager.class)//
		      .req(ReportServiceManager.class, ProjectService.class)//
		      .req(AppDataComparisonNotifier.class, ServerConfigManager.class));

		all.add(C(TaskBuilder.class, AppDatabasePruner.ID, AppDatabasePruner.class).req(AppCommandDataDao.class,
		      AppSpeedDataDao.class, AppSpeedConfigManager.class, AppConfigManager.class));

		all.add(C(ReportFacade.class));

		all.add(C(CachedReportTask.class).req(ReportServiceManager.class, ServerConfigManager.class)
		      .req(TaskBuilder.class, TransactionReportBuilder.ID, "m_transactionReportBuilder")
		      .req(TaskBuilder.class, EventReportBuilder.ID, "m_eventReportBuilder")
		      .req(TaskBuilder.class, ProblemReportBuilder.ID, "m_problemReportBuilder")
		      .req(TaskBuilder.class, CrossReportBuilder.ID, "m_crossReportBuilder")
		      .req(TaskBuilder.class, MatrixReportBuilder.ID, "m_matrixReportBuilder"));

		return all;
	}
}
