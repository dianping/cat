package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.dal.LogviewDao;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.hdfs.InputChannelManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.event.CompositeEventService;
import com.dianping.cat.report.page.model.event.HistoricalEventService;
import com.dianping.cat.report.page.model.event.LocalEventService;
import com.dianping.cat.report.page.model.heartbeat.CompositeHeartbeatService;
import com.dianping.cat.report.page.model.heartbeat.HistoricalHeartbeatService;
import com.dianping.cat.report.page.model.heartbeat.LocalHeartbeatService;
import com.dianping.cat.report.page.model.ip.CompositeIpService;
import com.dianping.cat.report.page.model.ip.HistoricalIpService;
import com.dianping.cat.report.page.model.ip.LocalIpService;
import com.dianping.cat.report.page.model.logview.CompositeLogViewService;
import com.dianping.cat.report.page.model.logview.HistoricalLogViewService;
import com.dianping.cat.report.page.model.logview.LocalLogViewService;
import com.dianping.cat.report.page.model.matrix.CompositeMatrixService;
import com.dianping.cat.report.page.model.matrix.HistoricalMatrixService;
import com.dianping.cat.report.page.model.matrix.LocalMatrixService;
import com.dianping.cat.report.page.model.problem.CompositeProblemService;
import com.dianping.cat.report.page.model.problem.HistoricalProblemService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionService;
import com.dianping.cat.report.page.model.transaction.HistoricalTransactionService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ModelService.class, "transaction-local", LocalTransactionService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "transaction", CompositeTransactionService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

		all.add(C(ModelService.class, "event-local", LocalEventService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "event", CompositeEventService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(C(ModelService.class, "problem-local", LocalProblemService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "problem", CompositeProblemService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "problem-historical" }, "m_services"));

		all.add(C(ModelService.class, "heartbeat-local", LocalHeartbeatService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "heartbeat", CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(ModelService.class, "matrix-local", LocalMatrixService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "matrix-historical", HistoricalMatrixService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "matrix", CompositeMatrixService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "matrix-historical" }, "m_services"));

		all.add(C(ModelService.class, "ip-local", LocalIpService.class) //
		      .req(BucketManager.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "ip-historical", HistoricalIpService.class) //
		      .req(BucketManager.class, ReportDao.class));
		all.add(C(ModelService.class, "ip", CompositeIpService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "ip-historical" }, "m_services"));

		all.add(C(ModelService.class, "logview-local", LocalLogViewService.class) //
		      .req(MessageConsumer.class, "realtime") //
		      .req(BucketManager.class) //
		      .req(MessageCodec.class, "html"));
		all.add(C(ModelService.class, "logview-historical", HistoricalLogViewService.class) //
		      .req(BucketManager.class, LogviewDao.class, InputChannelManager.class) //
		      .req(MessageCodec.class, "html"));
		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "logview-historical" }, "m_services"));

		return all;
	}
}
