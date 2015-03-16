package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
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
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.core.HtmlMessageCodec;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.message.spi.core.WaterfallMessageCodec;
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
import com.dianping.cat.report.page.model.state.CompositeStateService;
import com.dianping.cat.report.page.model.state.HistoricalStateService;
import com.dianping.cat.report.page.model.state.LocalStateService;
import com.dianping.cat.report.page.model.storage.CompositeStorageService;
import com.dianping.cat.report.page.model.storage.HistoricalStorageService;
import com.dianping.cat.report.page.model.storage.LocalStorageService;
import com.dianping.cat.report.page.model.top.CompositeTopService;
import com.dianping.cat.report.page.model.top.HistoricalTopService;
import com.dianping.cat.report.page.model.top.LocalTopService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionService;
import com.dianping.cat.report.page.model.transaction.HistoricalTransactionService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.report.service.impl.CrossReportService;
import com.dianping.cat.report.service.impl.DependencyReportService;
import com.dianping.cat.report.service.impl.EventReportService;
import com.dianping.cat.report.service.impl.HeartbeatReportService;
import com.dianping.cat.report.service.impl.MatrixReportService;
import com.dianping.cat.report.service.impl.MetricReportService;
import com.dianping.cat.report.service.impl.ProblemReportService;
import com.dianping.cat.report.service.impl.StateReportService;
import com.dianping.cat.report.service.impl.StorageReportService;
import com.dianping.cat.report.service.impl.TopReportService;
import com.dianping.cat.report.service.impl.TransactionReportService;
import com.dianping.cat.storage.message.MessageBucketManager;
import com.dianping.cat.storage.report.ReportBucketManager;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ModelService.class, "transaction-local", LocalTransactionService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
		      .req(ReportBucketManager.class, TransactionReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TransactionAnalyzer.ID, CompositeTransactionService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

		all.add(C(ModelService.class, "event-local", LocalEventService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(ReportBucketManager.class, EventReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, EventAnalyzer.ID, CompositeEventService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(C(ModelService.class, "problem-local", LocalProblemService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
		      .req(ReportBucketManager.class, ProblemReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, ProblemAnalyzer.ID, CompositeProblemService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "problem-historical" }, "m_services"));

		all.add(C(ModelService.class, "heartbeat-local", LocalHeartbeatService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(ReportBucketManager.class, HeartbeatReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(ModelService.class, "matrix-local", LocalMatrixService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "matrix-historical", HistoricalMatrixService.class) //
		      .req(ReportBucketManager.class, MatrixReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, MatrixAnalyzer.ID, CompositeMatrixService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "matrix-historical" }, "m_services"));

		all.add(C(ModelService.class, "state-local", LocalStateService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "state-historical", HistoricalStateService.class) //
		      .req(ReportBucketManager.class, StateReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, StateAnalyzer.ID, CompositeStateService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "state-historical" }, "m_services"));

		all.add(C(ModelService.class, "cross-local", LocalCrossService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "cross-historical", HistoricalCrossService.class) //
		      .req(ReportBucketManager.class, CrossReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, CrossAnalyzer.ID, CompositeCrossService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "cross-historical" }, "m_services"));

		all.add(C(ModelService.class, "top-local", LocalTopService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "top-historical", HistoricalTopService.class) //
		      .req(ReportBucketManager.class, TopReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TopAnalyzer.ID, CompositeTopService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "top-historical" }, "m_services"));

		all.add(C(ModelService.class, "dependency-local", LocalDependencyService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "dependency-historical", HistoricalDependencyService.class) //
		      .req(ReportBucketManager.class, DependencyReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, DependencyAnalyzer.ID, CompositeDependencyService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "dependency-historical" }, "m_services"));

		all.add(C(ModelService.class, "storage-local", LocalStorageService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "storage-historical", HistoricalStorageService.class) //
		      .req(ReportBucketManager.class, StorageReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, StorageAnalyzer.ID, CompositeStorageService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "storage-historical" }, "m_services"));

		all.add(C(ModelService.class, "metric-local", LocalMetricService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "metric-historical", HistoricalMetricService.class) //
		      .req(ReportBucketManager.class, MetricReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, MetricAnalyzer.ID, CompositeMetricService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "metric-historical" }, "m_services"));

		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "message-historical", "logview-historical" }, "m_services"));

		all.add(C(ModelService.class, "message-local", LocalMessageService.class) //
		      .req(MessageConsumer.class, ServerConfigManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID, "m_html") //
		      .req(MessageCodec.class, WaterfallMessageCodec.ID, "m_waterfall"));
		all.add(C(ModelService.class, "message-historical", HistoricalMessageService.class) //
		      .req(MessageBucketManager.class, HdfsMessageBucketManager.ID) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID, "m_html") //
		      .req(MessageCodec.class, WaterfallMessageCodec.ID, "m_waterfall").req(ServerConfigManager.class));
		
		return all;
	}
}
