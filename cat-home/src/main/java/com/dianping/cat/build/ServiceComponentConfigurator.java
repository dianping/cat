package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.page.cross.service.CompositeCrossService;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.cross.service.HistoricalCrossService;
import com.dianping.cat.report.page.cross.service.LocalCrossService;
import com.dianping.cat.report.page.dependency.service.CompositeDependencyService;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.dependency.service.HistoricalDependencyService;
import com.dianping.cat.report.page.dependency.service.LocalDependencyService;
import com.dianping.cat.report.page.event.service.CompositeEventService;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.service.HistoricalEventService;
import com.dianping.cat.report.page.event.service.LocalEventService;
import com.dianping.cat.report.page.heartbeat.service.CompositeHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.heartbeat.service.HistoricalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.report.page.logview.service.CompositeLogViewService;
import com.dianping.cat.report.page.logview.service.HistoricalMessageService;
import com.dianping.cat.report.page.logview.service.LocalMessageService;
import com.dianping.cat.report.page.matrix.service.CompositeMatrixService;
import com.dianping.cat.report.page.matrix.service.HistoricalMatrixService;
import com.dianping.cat.report.page.matrix.service.LocalMatrixService;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.metric.service.CompositeMetricService;
import com.dianping.cat.report.page.metric.service.HistoricalMetricService;
import com.dianping.cat.report.page.metric.service.LocalMetricService;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.problem.service.CompositeProblemService;
import com.dianping.cat.report.page.problem.service.HistoricalProblemService;
import com.dianping.cat.report.page.problem.service.LocalProblemService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.state.service.CompositeStateService;
import com.dianping.cat.report.page.state.service.HistoricalStateService;
import com.dianping.cat.report.page.state.service.LocalStateService;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.storage.service.CompositeStorageService;
import com.dianping.cat.report.page.storage.service.HistoricalStorageService;
import com.dianping.cat.report.page.storage.service.LocalStorageService;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.top.service.CompositeTopService;
import com.dianping.cat.report.page.top.service.HistoricalTopService;
import com.dianping.cat.report.page.top.service.LocalTopService;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.service.CompositeTransactionService;
import com.dianping.cat.report.page.transaction.service.HistoricalTransactionService;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.IpService;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(LocalModelService.class, LocalTransactionService.ID, LocalTransactionService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
		      .req(TransactionReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TransactionAnalyzer.ID, CompositeTransactionService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalEventService.ID, LocalEventService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(EventReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, EventAnalyzer.ID, CompositeEventService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalProblemService.ID, LocalProblemService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
		      .req(ProblemReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, ProblemAnalyzer.ID, CompositeProblemService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "problem-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalHeartbeatService.ID, LocalHeartbeatService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(HeartbeatReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalMatrixService.ID, LocalMatrixService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "matrix-historical", HistoricalMatrixService.class) //
		      .req(MatrixReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, MatrixAnalyzer.ID, CompositeMatrixService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "matrix-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalStateService.ID, LocalStateService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "state-historical", HistoricalStateService.class) //
		      .req(StateReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, StateAnalyzer.ID, CompositeStateService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "state-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalCrossService.ID, LocalCrossService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "cross-historical", HistoricalCrossService.class) //
		      .req(CrossReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, CrossAnalyzer.ID, CompositeCrossService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "cross-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalTopService.ID, LocalTopService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "top-historical", HistoricalTopService.class) //
		      .req(TopReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TopAnalyzer.ID, CompositeTopService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "top-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalDependencyService.ID, LocalDependencyService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "dependency-historical", HistoricalDependencyService.class) //
		      .req(DependencyReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, DependencyAnalyzer.ID, CompositeDependencyService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "dependency-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalStorageService.ID, LocalStorageService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "storage-historical", HistoricalStorageService.class) //
		      .req(StorageReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, StorageAnalyzer.ID, CompositeStorageService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "storage-historical" }, "m_services"));

		all.add(C(LocalModelService.class, LocalMetricService.ID, LocalMetricService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class, IpService.class));
		all.add(C(ModelService.class, "metric-historical", HistoricalMetricService.class) //
		      .req(MetricReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, MetricAnalyzer.ID, CompositeMetricService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "metric-historical" }, "m_services"));

		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "logview-historical", "logview-local" }, "m_services"));

		all.add(C(LocalModelService.class, "logview", LocalMessageService.class) //
		      .req(MessageConsumer.class, ServerConfigManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID, "m_html") //
		      .req(MessageCodec.class, WaterfallMessageCodec.ID, "m_waterfall"));
		all.add(C(ModelService.class, "logview-local", LocalMessageService.class) //
		      .req(MessageConsumer.class, ServerConfigManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID, "m_html") //
		      .req(MessageCodec.class, WaterfallMessageCodec.ID, "m_waterfall"));
		all.add(C(ModelService.class, "logview-historical", HistoricalMessageService.class) //
		      .req(MessageBucketManager.class, HdfsMessageBucketManager.ID) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID, "m_html") //
		      .req(MessageCodec.class, WaterfallMessageCodec.ID, "m_waterfall").req(ServerConfigManager.class));

		return all;
	}
}
