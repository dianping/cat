package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.report.page.model.cross.CompositeCrossService;
import com.dianping.cat.report.page.model.cross.HistoricalCrossService;
import com.dianping.cat.report.page.model.cross.LocalCrossService;
import com.dianping.cat.report.page.model.dependency.CompositeDependencyService;
import com.dianping.cat.report.page.model.dependency.HistoricalDependencyService;
import com.dianping.cat.report.page.model.dependency.LocalDependencyService;
import com.dianping.cat.report.page.model.event.CompositeEventService;
import com.dianping.cat.report.page.model.event.HistoricalEventService;
import com.dianping.cat.report.page.model.event.LocalEventService;
import com.dianping.cat.report.page.model.heartbeat.CompositeHeartbeatService;
import com.dianping.cat.report.page.model.heartbeat.HistoricalHeartbeatService;
import com.dianping.cat.report.page.model.heartbeat.LocalHeartbeatService;
import com.dianping.cat.report.page.model.logview.CompositeLogViewService;
import com.dianping.cat.report.page.model.logview.HistoricalMessageService;
import com.dianping.cat.report.page.model.logview.LocalMessageService;
import com.dianping.cat.report.page.model.matrix.CompositeMatrixService;
import com.dianping.cat.report.page.model.matrix.HistoricalMatrixService;
import com.dianping.cat.report.page.model.matrix.LocalMatrixService;
import com.dianping.cat.report.page.model.metric.CompositeMetricService;
import com.dianping.cat.report.page.model.metric.HistoricalMetricService;
import com.dianping.cat.report.page.model.metric.LocalMetricService;
import com.dianping.cat.report.page.model.problem.CompositeProblemService;
import com.dianping.cat.report.page.model.problem.HistoricalProblemService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.sql.CompositeSqlService;
import com.dianping.cat.report.page.model.sql.HistoricalSqlService;
import com.dianping.cat.report.page.model.sql.LocalSqlService;
import com.dianping.cat.report.page.model.state.CompositeStateService;
import com.dianping.cat.report.page.model.state.HistoricalStateService;
import com.dianping.cat.report.page.model.state.LocalStateService;
import com.dianping.cat.report.page.model.top.CompositeTopService;
import com.dianping.cat.report.page.model.top.HistoricalTopService;
import com.dianping.cat.report.page.model.top.LocalTopService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionService;
import com.dianping.cat.report.page.model.transaction.HistoricalTransactionService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.report.service.CachedReportTask;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ModelService.class, "transaction-local", LocalTransactionService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, TransactionAnalyzer.ID, CompositeTransactionService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

		all.add(C(ModelService.class, "event-local", LocalEventService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, EventAnalyzer.ID, CompositeEventService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(C(ModelService.class, "problem-local", LocalProblemService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, ProblemAnalyzer.ID, CompositeProblemService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "problem-historical" }, "m_services"));

		all.add(C(ModelService.class, "heartbeat-local", LocalHeartbeatService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(ModelService.class, "matrix-local", LocalMatrixService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "matrix-historical", HistoricalMatrixService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, MatrixAnalyzer.ID, CompositeMatrixService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "matrix-historical" }, "m_services"));

		all.add(C(ModelService.class, "state-local", LocalStateService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "state-historical", HistoricalStateService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, StateAnalyzer.ID, CompositeStateService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "state-historical" }, "m_services"));

		all.add(C(ModelService.class, "cross-local", LocalCrossService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "cross-historical", HistoricalCrossService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, CrossAnalyzer.ID, CompositeCrossService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "cross-historical" }, "m_services"));

		all.add(C(ModelService.class, "sql-local", LocalSqlService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "sql-historical", HistoricalSqlService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, SqlAnalyzer.ID, CompositeSqlService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "sql-historical" }, "m_services"));

		all.add(C(ModelService.class, "top-local", LocalTopService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "top-historical", HistoricalTopService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, TopAnalyzer.ID, CompositeTopService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "top-historical" }, "m_services"));

		all.add(C(ModelService.class, "dependency-local", LocalDependencyService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "dependency-historical", HistoricalDependencyService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, DependencyAnalyzer.ID, CompositeDependencyService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "dependency-historical" }, "m_services"));

		all.add(C(ModelService.class, "metric-local", LocalMetricService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class));
		all.add(C(ModelService.class, "metric-historical", HistoricalMetricService.class) //
		      .req(BucketManager.class, ReportService.class));
		all.add(C(ModelService.class, MetricAnalyzer.ID, CompositeMetricService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "metric-historical" }, "m_services"));

		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "message-historical", "logview-historical" }, "m_services"));

		all.add(C(ModelService.class, "message-local", LocalMessageService.class) //
		      .req(MessageConsumer.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID) //
		      .req(MessageCodec.class, "html"));
		all.add(C(ModelService.class, "message-historical", HistoricalMessageService.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID, "m_localBucketManager") //
		      .req(MessageBucketManager.class, HdfsMessageBucketManager.ID, "m_hdfsBucketManager") //
		      .req(MessageCodec.class, "html"));

		all.add(C(CachedReportTask.class).req(ReportService.class));
		return all;
	}
}
