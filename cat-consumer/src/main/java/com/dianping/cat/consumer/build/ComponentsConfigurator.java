package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.SqltableDao;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.DefaultAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.common.StateAnalyzer;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.database.DatabaseAnalyzer;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.DumpUploader;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.ip.TopIpAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.handler.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.handler.LongExecutionHandler;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.SqlParseManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;
import org.unidal.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AnalyzerFactory.class, DefaultAnalyzerFactory.class));

		all.add(C(SqlParseManager.class, SqlParseManager.class)//
		      .req(SqltableDao.class));

		all.add(C(MessageConsumer.class, "realtime", RealtimeConsumer.class) //
		      .req(AnalyzerFactory.class, ServerStateManager.class) //
		      .config(E("extraTime").value(property("extraTime", "180000"))//
		            , E("analyzers").value("problem,transaction,event,heartbeat,matrix,cross,database,sql,dump,common")));

		String errorTypes = "Error,RuntimeException,Exception";
		String failureTypes = "URL,SQL,Call,PigeonCall,Cache";

		all.add(C(Handler.class, "DefaultHandler", DefaultProblemHandler.class)//
		      .config(E("failureType").value(failureTypes))//
		      .config(E("errorType").value(errorTypes)));

		all.add(C(Handler.class, "LongHandler", LongExecutionHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class) //
		      .req(Handler.class, new String[] { "DefaultHandler", "LongHandler" }, "m_handlers"));

		all.add(C(TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(EventAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(CrossAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(DatabaseAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, SqlParseManager.class));

		all.add(C(SqlAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, SqlParseManager.class));

		all.add(C(MatrixAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(StateAnalyzer.class).is(PER_LOOKUP)//
		      .req(HostinfoDao.class, TaskDao.class, ReportDao.class, ProjectDao.class)//
		      .req(BucketManager.class));

		all.add(C(TopIpAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(HeartbeatAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(DumpAnalyzer.class).is(PER_LOOKUP) //
		      .req(ServerConfigManager.class) //
		      .req(DumpUploader.class)//
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID));

		all.add(C(DumpUploader.class) //
		      .req(ServerConfigManager.class, FileSystemManager.class));

		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));

		// database
		all.add(C(JdbcDataSourceConfigurationManager.class).config(
		      E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
