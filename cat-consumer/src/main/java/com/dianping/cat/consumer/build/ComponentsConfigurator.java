package com.dianping.cat.consumer.build;

import static com.dianping.cat.consumer.problem.ProblemType.ERROR;
import static com.dianping.cat.consumer.problem.ProblemType.FAILURE;
import static com.dianping.cat.consumer.problem.ProblemType.HEARTBEAT;
import static com.dianping.cat.consumer.problem.ProblemType.LONG_URL;
import static com.dianping.cat.consumer.problem.ProblemType.LONG_SQL;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.DefaultAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.DumpChannel;
import com.dianping.cat.consumer.dump.DumpChannelManager;
import com.dianping.cat.consumer.dump.DumpUploader;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.ip.TopIpAnalyzer;
import com.dianping.cat.consumer.logview.LogviewUploader;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.handler.ErrorHandler;
import com.dianping.cat.consumer.problem.handler.FailureHandler;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.handler.HeartbeatHandler;
import com.dianping.cat.consumer.problem.handler.LongSqlHandler;
import com.dianping.cat.consumer.problem.handler.LongUrlHandler;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.hadoop.dal.LogviewDao;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AnalyzerFactory.class, DefaultAnalyzerFactory.class));

		all.add(C(MessageConsumer.class, "realtime", RealtimeConsumer.class) //
		      .req(AnalyzerFactory.class, LogviewUploader.class) //
		      .config(E("extraTime").value(property("extraTime", "180000"))//
		            , E("analyzers").value("problem,transaction,event,ip,heartbeat,dump")));

		String errorTypes = "Error,RuntimeException,Exception";
		String failureTypes = "URL,SQL,Call,Cache";
		
		all.add(C(Handler.class, HEARTBEAT.getName(), HeartbeatHandler.class));

		all.add(C(Handler.class, ERROR.getName(), ErrorHandler.class)//
		      .config(E("errorType").value(errorTypes)));

		all.add(C(Handler.class, FAILURE.getName(), FailureHandler.class)//
		      .config(E("failureType").value(failureTypes)));

		all.add(C(Handler.class, LONG_URL.getName(), LongUrlHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(Handler.class, LONG_SQL.getName(), LongSqlHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(Handler.class, new String[] { FAILURE.getName(), ERROR.getName(), LONG_URL.getName() ,LONG_SQL.getName() ,HEARTBEAT.getName() }, "m_handlers") //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class));

		all.add(C(EventAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));
		
		all.add(C(TopIpAnalyzer.class).is(PER_LOOKUP) //
				.req(BucketManager.class, ReportDao.class));

		all.add(C(HeartbeatAnalyzer.class).is(PER_LOOKUP) //
				.req(BucketManager.class, ReportDao.class));

		all.add(C(DumpAnalyzer.class).is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class) //
		      .req(DumpUploader.class, DumpChannelManager.class));

		all.add(C(DumpChannel.class));
		all.add(C(DumpChannelManager.class) //
		      .req(MessageCodec.class, "plain-text"));

		all.add(C(DumpUploader.class) //
		      .req(ServerConfigManager.class, FileSystemManager.class)); //
		all.add(C(LogviewUploader.class) //
		      .req(ServerConfigManager.class, FileSystemManager.class) //
		      .req(BucketManager.class, LogviewDao.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
