package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.core.dal.DailyReportDao;
import com.dianping.cat.consumer.core.dal.DailyGraphDao;
import com.dianping.cat.consumer.core.dal.GraphDao;
import com.dianping.cat.consumer.core.dal.MonthlyReportDao;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.core.dal.WeeklyReportDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.cross.CrossMerger;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;
import com.dianping.cat.report.task.event.EventGraphCreator;
import com.dianping.cat.report.task.event.EventMerger;
import com.dianping.cat.report.task.event.EventReportBuilder;
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
import com.dianping.cat.report.task.state.StateMerger;
import com.dianping.cat.report.task.state.StateReportBuilder;
import com.dianping.cat.report.task.thread.DefaultTaskConsumer;
import com.dianping.cat.report.task.thread.TaskProducer;
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
		all.add(C(HeartbeatMerger.class));
		all.add(C(CrossMerger.class));
		all.add(C(MatrixMerger.class));
		all.add(C(SqlMerger.class));
		all.add(C(StateMerger.class));

		all.add(C(TransactionReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class,
		            TransactionGraphCreator.class)//
		      .req(TransactionMerger.class, WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(EventReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, EventGraphCreator.class,
		            EventMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(ProblemReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, ProblemGraphCreator.class) //
		      .req(WeeklyReportDao.class, MonthlyReportDao.class, ProblemMerger.class));

		all.add(C(HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class) //
		      .req(HeartbeatGraphCreator.class, HeartbeatMerger.class, WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(MatrixReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, MatrixMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(SqlReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, SqlMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(CrossReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, CrossMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(CrossReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, CrossMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(StateReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class, StateMerger.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(DependencyReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ReportDao.class, DailyReportDao.class)//
		      .req(WeeklyReportDao.class, MonthlyReportDao.class)//
		      .req(ReportService.class, TopologyGraphBuilder.class, TopologyGraphDao.class));

		all.add(C(TaskProducer.class, TaskProducer.class) //
		      .req(TaskDao.class, ReportService.class));

		all.add(C(ReportFacade.class)//
		      .req(TransactionReportBuilder.class, EventReportBuilder.class, ProblemReportBuilder.class //
		            ,HeartbeatReportBuilder.class, MatrixReportBuilder.class, CrossReportBuilder.class //
		            ,SqlReportBuilder.class,StateReportBuilder.class, DependencyReportBuilder.class));

		return all;
	}
}
