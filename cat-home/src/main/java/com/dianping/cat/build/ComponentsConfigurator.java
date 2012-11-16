package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.CatHomeModule;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.GraphDao;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.WeeklyreportDao;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.dianping.cat.report.page.cross.DomainManager;
import com.dianping.cat.report.page.health.HistoryGraphs;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.report.service.impl.DailyReportServiceImpl;
import com.dianping.cat.report.task.cross.CrossMerger;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.database.DatabaseMerger;
import com.dianping.cat.report.task.database.DatabaseReportBuilder;
import com.dianping.cat.report.task.event.EventGraphCreator;
import com.dianping.cat.report.task.event.EventMerger;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.health.HealthReportBuilder;
import com.dianping.cat.report.task.health.HealthServiceCollector;
import com.dianping.cat.report.task.heartbeat.HeartbeatGraphCreator;
import com.dianping.cat.report.task.heartbeat.HeartbeatMerger;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixMerger;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.problem.ProblemGraphCreator;
import com.dianping.cat.report.task.problem.ProblemMerger;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.spi.ReportFacade;
import com.dianping.cat.report.task.sql.SqlMerger;
import com.dianping.cat.report.task.sql.SqlReportBuilder;
import com.dianping.cat.report.task.thread.TaskProducer;
import com.dianping.cat.report.task.thread.DefaultTaskConsumer;
import com.dianping.cat.report.task.transaction.TransactionGraphCreator;
import com.dianping.cat.report.task.transaction.TransactionMerger;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.dianping.cat.report.view.DomainNavManager;
import com.site.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import com.site.initialization.DefaultModuleManager;
import com.site.initialization.Module;
import com.site.initialization.ModuleManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
		      .req(MessageConsumer.class, new String[] { "realtime" }, "m_consumers"));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.add(C(DefaultTaskConsumer.class) //
		      .req(TaskDao.class, ReportFacade.class));

		all.add(C(TransactionGraphCreator.class));
		all.add(C(EventGraphCreator.class));
		all.add(C(ProblemGraphCreator.class));
		all.add(C(HeartbeatGraphCreator.class));

		all.add(C(TransactionMerger.class));
		all.add(C(EventMerger.class));
		all.add(C(ProblemMerger.class));
		all.add(C(HeartbeatMerger.class));
		all.add(C(CrossMerger.class));
		all.add(C(DatabaseMerger.class));
		all.add(C(SqlMerger.class));

		all.add(C(TransactionReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, TransactionGraphCreator.class)//
		      .req(TransactionMerger.class, WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(EventReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, EventGraphCreator.class, EventMerger.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(ProblemReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, ProblemGraphCreator.class) //
		      .req(WeeklyreportDao.class, MonthreportDao.class, ProblemMerger.class));

		all.add(C(HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, HeartbeatGraphCreator.class).req(
		            HeartbeatMerger.class, WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(MatrixReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, MatrixMerger.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(DatabaseReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, DatabaseMerger.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(SqlReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, SqlMerger.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(CrossReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class, CrossMerger.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class));

		all.add(C(TaskProducer.class, TaskProducer.class) //
		      .req(TaskDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(HealthReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class)//
		      .req(WeeklyreportDao.class, MonthreportDao.class,HealthServiceCollector.class));

		all.add(C(ReportFacade.class)//
		      .req(TransactionReportBuilder.class, EventReportBuilder.class, ProblemReportBuilder.class,//
		            HeartbeatReportBuilder.class, MatrixReportBuilder.class, CrossReportBuilder.class,//
		            DatabaseReportBuilder.class, SqlReportBuilder.class, HealthReportBuilder.class,//
		            TaskDao.class));

		all.add(C(DomainManager.class, DomainManager.class).req(ServerConfigManager.class, HostinfoDao.class));

		all.add(C(HealthServiceCollector.class).req(DomainManager.class, ReportDao.class));

		all.add(C(HistoryGraphs.class, HistoryGraphs.class).//
		      req(ReportDao.class, DailyreportDao.class));

		all.add(C(Module.class, CatHomeModule.ID, CatHomeModule.class));
		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));

		all.add(C(DomainNavManager.class).req(ProjectDao.class, ServerConfigManager.class));

		all.add(C(DailyReportService.class, DailyReportServiceImpl.class)//
		      .req(DailyreportDao.class));

		// model service
		all.addAll(new ServiceComponentConfigurator().defineComponents());

		// database
		all.add(C(JdbcDataSourceConfigurationManager.class).config(
		      E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.addAll(new UserDatabaseConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());
		return all;
	}
}
