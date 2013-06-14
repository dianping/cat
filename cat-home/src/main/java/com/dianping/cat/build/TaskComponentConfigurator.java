package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.cross.CompositeCrossService;
import com.dianping.cat.report.page.model.cross.HistoricalCrossService;
import com.dianping.cat.report.page.model.cross.LocalCrossService;
import com.dianping.cat.report.page.model.database.CompositeDatabaseService;
import com.dianping.cat.report.page.model.database.HistoricalDatabaseService;
import com.dianping.cat.report.page.model.database.LocalDatabaseService;
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
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

class TaskComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();


		return all;
	}
}
