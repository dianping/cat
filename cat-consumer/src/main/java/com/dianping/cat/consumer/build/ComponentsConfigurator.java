package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.core.DumpAnalyzer;
import com.dianping.cat.consumer.core.EventAnalyzer;
import com.dianping.cat.consumer.core.HeartbeatAnalyzer;
import com.dianping.cat.consumer.core.ProblemAnalyzer;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.core.StateAnalyzer;
import com.dianping.cat.consumer.core.TopAnalyzer;
import com.dianping.cat.consumer.core.aggregation.AggregationConfigManager;
import com.dianping.cat.consumer.core.aggregation.AggregationHandler;
import com.dianping.cat.consumer.core.aggregation.DefaultAggregationHandler;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.dal.HostinfoDao;
import com.dianping.cat.consumer.core.dal.ProjectDao;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.core.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.core.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.core.problem.ProblemHandler;
import com.dianping.cat.consumer.core.problem.ProblemReportAggregation;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.service.DefaultReportManager;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AggregationHandler.class, DefaultAggregationHandler.class));

		all.add(C(AggregationConfigManager.class).req(AggregationHandler.class, ConfigDao.class));

		all.add(C(ProblemReportAggregation.class).req(AggregationConfigManager.class));

		all.add(C(MessageConsumer.class, RealtimeConsumer.class) //
		      .req(MessageAnalyzerManager.class, ServerStatisticManager.class));

		all.addAll(defineTransactionComponents());

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
		      .config(E("failureType").value("URL,SQL,Call,PigeonCall,Cache"))//
		      .config(E("errorType").value("Error,RuntimeException,Exception")));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ProblemAnalyzer.ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class, ProblemReportAggregation.class) //
		      .req(ProblemHandler.class, new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));

		all.add(C(MessageAnalyzer.class, EventAnalyzer.ID, EventAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(MessageAnalyzer.class, StateAnalyzer.ID, StateAnalyzer.class).is(PER_LOOKUP)//
		      .req(HostinfoDao.class, ReportDao.class, ProjectDao.class)//
		      .req(BucketManager.class, ServerStatisticManager.class));

		all.add(C(MessageAnalyzer.class, HeartbeatAnalyzer.ID, HeartbeatAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(MessageAnalyzer.class, DumpAnalyzer.ID, DumpAnalyzer.class).is(PER_LOOKUP) //
		      .req(ServerStatisticManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID));

		all.add(C(MessageAnalyzer.class, TopAnalyzer.ID, TopAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(ProductLineConfigManager.class).req(ConfigDao.class));

		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));

		return all;
	}

	private Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, TransactionDelegate.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
