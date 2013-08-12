package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineCreator;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.baseline.impl.DefaultBaselineCreator;
import com.dianping.cat.report.baseline.impl.DefaultBaselineService;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.bug.BugReportBuilder;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;
import com.dianping.cat.report.task.event.EventGraphCreator;
import com.dianping.cat.report.task.event.EventMerger;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatGraphCreator;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.metric.MetricAlert;
import com.dianping.cat.report.task.metric.MetricBaselineReportBuilder;
import com.dianping.cat.report.task.metric.MetricPointParser;
import com.dianping.cat.report.task.problem.ProblemGraphCreator;
import com.dianping.cat.report.task.problem.ProblemMerger;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.spi.ReportFacade;
import com.dianping.cat.report.task.sql.SqlMerger;
import com.dianping.cat.report.task.sql.SqlReportBuilder;
import com.dianping.cat.report.task.state.StateReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionGraphCreator;
import com.dianping.cat.report.task.transaction.TransactionMerger;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;

public class TaskComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultTaskConsumer.class) //
		      .req(TaskDao.class, ReportFacade.class));

		all.add(C(TransactionGraphCreator.class));
		all.add(C(EventGraphCreator.class));
		all.add(C(ProblemGraphCreator.class));
		all.add(C(HeartbeatGraphCreator.class));

		all.add(C(TransactionMerger.class));
		all.add(C(EventMerger.class));
		all.add(C(ProblemMerger.class));
		all.add(C(SqlMerger.class));

		all.add(C(MetricPointParser.class));
		all.add(C(BaselineCreator.class, DefaultBaselineCreator.class));
		all.add(C(BaselineService.class, DefaultBaselineService.class).req(BaselineDao.class));
		all.add(C(BaselineConfigManager.class, BaselineConfigManager.class));

		all.add(C(MetricBaselineReportBuilder.class).req(ReportService.class, MetricPointParser.class)//
		      .req(MetricConfigManager.class, ProductLineConfigManager.class)//
		      .req(BaselineCreator.class, BaselineService.class, BaselineConfigManager.class));

		all.add(C(MetricAlert.class).req(ReportService.class, ServerConfigManager.class, MetricPointParser.class)//
		      .req(MetricConfigManager.class, ProductLineConfigManager.class)//
		      .req(BaselineService.class, BaselineConfigManager.class)//
		      .req(ModelService.class, "metric"));

		all.add(C(TransactionReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportService.class)//
		      .req(TransactionGraphCreator.class, TransactionMerger.class));

		all.add(C(EventReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportService.class)//
		      .req(EventGraphCreator.class, EventMerger.class));//

		all.add(C(ProblemReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportService.class)//
		      .req(ProblemGraphCreator.class, ProblemMerger.class));

		all.add(C(HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, ReportService.class) //
		      .req(HeartbeatGraphCreator.class));

		all.add(C(BugReportBuilder.class)
		      .req( ReportService.class));

		all.add(C(MatrixReportBuilder.class).req(ReportService.class));

		all.add(C(SqlReportBuilder.class).req(ReportService.class, SqlMerger.class));

		all.add(C(CrossReportBuilder.class).req(ReportService.class));

		all.add(C(CrossReportBuilder.class).req(ReportService.class));

		all.add(C(StateReportBuilder.class).req(ReportService.class));

		all.add(C(DependencyReportBuilder.class).req(ReportService.class, TopologyGraphBuilder.class,
		      TopologyGraphDao.class));

		all.add(C(ReportFacade.class)//
		      .req(TransactionReportBuilder.class, EventReportBuilder.class, ProblemReportBuilder.class //
		      		, HeartbeatReportBuilder.class, MatrixReportBuilder.class, CrossReportBuilder.class //
		      		, SqlReportBuilder.class, StateReportBuilder.class, DependencyReportBuilder.class,BugReportBuilder.class,
		            MetricBaselineReportBuilder.class));

		return all;
	}
}
