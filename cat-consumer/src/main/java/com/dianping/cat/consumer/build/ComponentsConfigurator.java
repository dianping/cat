package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dainping.cat.consumer.core.dal.HostinfoDao;
import com.dainping.cat.consumer.core.dal.ProjectDao;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.DefaultMessageAnalyzerManager;
import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerManager;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.core.DumpAnalyzer;
import com.dianping.cat.consumer.core.EventAnalyzer;
import com.dianping.cat.consumer.core.HeartbeatAnalyzer;
import com.dianping.cat.consumer.core.ProblemAnalyzer;
import com.dianping.cat.consumer.core.StateAnalyzer;
import com.dianping.cat.consumer.core.TopAnalyzer;
import com.dianping.cat.consumer.core.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.core.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.core.problem.ProblemHandler;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DefaultReportService;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.ReportService;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageAnalyzerManager.class, DefaultMessageAnalyzerManager.class));

		all.add(C(MessageConsumer.class, RealtimeConsumer.ID, RealtimeConsumer.class) //
		      .req(MessageAnalyzerManager.class, ServerStateManager.class));

		all.addAll(defineTransactionComponents());

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
		      .config(E("failureType").value("URL,SQL,Call,PigeonCall,Cache"))//
		      .config(E("errorType").value("Error,RuntimeException,Exception")));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ProblemAnalyzer.ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class) //
		      .req(ProblemHandler.class, new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));

		all.add(C(MessageAnalyzer.class, EventAnalyzer.ID, EventAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(MessageAnalyzer.class, StateAnalyzer.ID, StateAnalyzer.class).is(PER_LOOKUP)//
		      .req(HostinfoDao.class, TaskDao.class, ReportDao.class, ProjectDao.class)//
		      .req(BucketManager.class, ServerStateManager.class));

		all.add(C(MessageAnalyzer.class, HeartbeatAnalyzer.ID, HeartbeatAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(MessageAnalyzer.class, DumpAnalyzer.ID, DumpAnalyzer.class).is(PER_LOOKUP) //
		      .req(ServerStateManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID));

		all.add(C(MessageAnalyzer.class, TopAnalyzer.ID, TopAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));

		// database
		all.add(C(JdbcDataSourceConfigurationManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatCoreDatabaseConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class));
		all.add(C(ReportManager.class, DefaultReportManager.class) //
		      .req(ReportService.class, ID) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));
		all.add(C(ReportService.class, ID, DefaultReportService.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportDao.class));
		all.add(C(ReportDelegate.class, ID, TransactionDelegate.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
