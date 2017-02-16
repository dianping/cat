package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportContentDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.core.dal.WeeklyReportContentDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.report.alert.sender.sender.SenderManager;
import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.app.task.AppDatabasePruner;
import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.report.page.app.task.CommandAutoCompleter;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.dependency.task.DependencyReportBuilder;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.task.EventGraphCreator;
import com.dianping.cat.report.page.event.task.EventMerger;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.BaselineCreator;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.page.metric.task.MetricBaselineReportBuilder;
import com.dianping.cat.report.page.metric.task.MetricPointParser;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.page.network.service.NetTopologyReportService;
import com.dianping.cat.report.page.network.task.NetTopologyReportBuilder;
import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.overload.task.CapacityUpdater;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.problem.task.ProblemGraphCreator;
import com.dianping.cat.report.page.problem.task.ProblemMerger;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.state.task.StateReportBuilder;
import com.dianping.cat.report.page.statistics.service.BugReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.SystemReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.statistics.task.bug.BugReportBuilder;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.report.page.statistics.task.system.SystemReportBuilder;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.task.TransactionGraphCreator;
import com.dianping.cat.report.page.transaction.task.TransactionMerger;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.cached.CachedReportBuilder;
import com.dianping.cat.report.task.cached.CachedReportTask;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.report.task.notify.NotifyTaskBuilder;
import com.dianping.cat.report.task.notify.ReportRender;
import com.dianping.cat.report.task.notify.ReportRenderImpl;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.service.RouterConfigService;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

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
		      .req(MetricReportService.class, MetricPointParser.class)//
		      .req(MetricConfigManager.class, ProductLineConfigManager.class)//
		      .req(BaselineCreator.class, BaselineService.class, BaselineConfigManager.class));

		all.add(C(TaskBuilder.class, TransactionReportBuilder.ID, TransactionReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, TransactionReportService.class)//
		      .req(TransactionGraphCreator.class, TransactionMerger.class));

		all.add(C(TaskBuilder.class, EventReportBuilder.ID, EventReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, EventReportService.class)//
		      .req(EventReportService.class).req(EventGraphCreator.class, EventMerger.class));//

		all.add(C(TaskBuilder.class, ProblemReportBuilder.ID, ProblemReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ProblemReportService.class)//
		      .req(ProblemGraphCreator.class, ProblemMerger.class));

		all.add(C(TaskBuilder.class, HeartbeatReportBuilder.ID, HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, HeartbeatReportService.class));

		all.add(C(TaskBuilder.class, BugReportBuilder.ID, BugReportBuilder.class).req(ProblemReportService.class,
		      BugReportService.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, ServiceReportBuilder.ID, ServiceReportBuilder.class).req(CrossReportService.class,
		      ServiceReportService.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, MatrixReportBuilder.ID, MatrixReportBuilder.class).req(MatrixReportService.class));

		all.add(C(TaskBuilder.class, CrossReportBuilder.ID, CrossReportBuilder.class).req(CrossReportService.class));

		all.add(C(TaskBuilder.class, StateReportBuilder.ID, StateReportBuilder.class) //
		      .req(ServerConfigManager.class, HostinfoService.class, ProjectService.class) //
		      .req(StateReportService.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, RouterConfigBuilder.ID, RouterConfigBuilder.class).req(RouterConfigService.class,
		      RouterConfigHandler.class));

		all.add(C(TaskBuilder.class, HeavyReportBuilder.ID, HeavyReportBuilder.class).req(MatrixReportService.class,
		      HeavyReportService.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, UtilizationReportBuilder.ID, UtilizationReportBuilder.class).req(
		      UtilizationReportService.class, TransactionReportService.class, HeartbeatReportService.class,
		      CrossReportService.class, TransactionMergeHelper.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, DependencyReportBuilder.ID, DependencyReportBuilder.class).req(
		      DependencyReportService.class, TopologyGraphBuilder.class, TopologyGraphDao.class));

		all.add(C(TaskBuilder.class, NetTopologyReportBuilder.ID, NetTopologyReportBuilder.class).req(
		      NetTopologyReportService.class, MetricReportService.class, NetGraphBuilder.class,
		      NetGraphConfigManager.class));

		all.add(C(TaskBuilder.class, JarReportBuilder.ID, JarReportBuilder.class).req(HeartbeatReportService.class,
		      JarReportService.class, ServerFilterConfigManager.class));

		all.add(C(TaskBuilder.class, SystemReportBuilder.ID, SystemReportBuilder.class).req(MetricReportService.class,
		      SystemReportService.class, ProductLineConfigManager.class));

		all.add(C(TaskBuilder.class, CachedReportBuilder.ID, CachedReportBuilder.class).req(CachedReportTask.class));

		all.add(C(TaskBuilder.class, StorageReportBuilder.ID, StorageReportBuilder.class).req(StorageReportService.class,
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

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(AppDatabaseConfigurator.class).req(AppCommandDataDao.class, AppSpeedDataDao.class));

		all.add(C(TaskBuilder.class, NotifyTaskBuilder.ID, NotifyTaskBuilder.class)
		      .req(ReportRender.class, SenderManager.class).req(ProjectService.class)
		      .req(TransactionReportService.class, EventReportService.class, ProblemReportService.class));

		all.add(C(TaskBuilder.class, AppDatabasePruner.ID, AppDatabasePruner.class).req(AppCommandDataDao.class,
		      AppSpeedDataDao.class, AppSpeedConfigManager.class, AppConfigManager.class));

		all.add(C(CommandAutoCompleter.class).req(TransactionReportService.class, AppConfigManager.class));

		all.add(C(TaskBuilder.class, AppReportBuilder.ID, AppReportBuilder.class).req(AppCommandDataDao.class,
		      AppConfigManager.class, AppReportService.class, TransactionReportService.class, CommandAutoCompleter.class));

		all.add(C(ReportFacade.class));

		all.add(C(CachedReportTask.class).req(ServerFilterConfigManager.class).req(TransactionReportService.class)
		      .req(TaskBuilder.class, TransactionReportBuilder.ID, "m_transactionReportBuilder")
		      .req(TaskBuilder.class, EventReportBuilder.ID, "m_eventReportBuilder")
		      .req(TaskBuilder.class, ProblemReportBuilder.ID, "m_problemReportBuilder")
		      .req(TaskBuilder.class, CrossReportBuilder.ID, "m_crossReportBuilder")
		      .req(TaskBuilder.class, MatrixReportBuilder.ID, "m_matrixReportBuilder"));

		return all;
	}
}
