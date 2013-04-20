package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.DefaultMessageAnalyzerFactory;
import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.handler.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.handler.LongExecutionHandler;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageAnalyzerFactory.class, DefaultMessageAnalyzerFactory.class));

		all.add(C(MessageConsumer.class, RealtimeConsumer.ID, RealtimeConsumer.class) //
		      .req(MessageAnalyzerFactory.class, ServerStateManager.class) //
		      .config(E("extraTime").value(property("extraTime", "180000")) //
		            , E("analyzers").value("problem,transaction,event,heartbeat,dump,state,top,cross,database,ip,matrix,sql")));

		all.add(C(Handler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
		      .config(E("failureType").value("URL,SQL,Call,PigeonCall,Cache"))//
		      .config(E("errorType").value("Error,RuntimeException,Exception")));

		all.add(C(Handler.class, LongExecutionHandler.ID, LongExecutionHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ProblemAnalyzer.ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class) //
		      .req(Handler.class, new String[] { DefaultProblemHandler.ID, LongExecutionHandler.ID }, "m_handlers"));

		all.add(C(MessageAnalyzer.class, TransactionAnalyzer.ID, TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

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

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
