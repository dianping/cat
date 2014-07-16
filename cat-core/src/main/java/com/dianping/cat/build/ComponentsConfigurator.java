package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.DomainManager;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.config.aggregation.AggregationHandler;
import com.dianping.cat.config.aggregation.DefaultAggregationHandler;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.url.DefaultUrlPatternHandler;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.config.url.UrlPatternHandler;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.core.DefaultMessageHandler;
import com.dianping.cat.message.spi.core.DefaultMessagePathBuilder;
import com.dianping.cat.message.spi.core.MessageHandler;
import com.dianping.cat.message.spi.core.MessagePathBuilder;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.storage.dump.LocalMessageBucket;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucket;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DomainManager.class)//
		      .req(ServerConfigManager.class, ProjectDao.class, HostinfoDao.class));

		all.add(C(TaskManager.class).req(TaskDao.class));
		all.add(C(ServerConfigManager.class));
		all.add(C(ServerStatisticManager.class));

		all.add(C(MessagePathBuilder.class, DefaultMessagePathBuilder.class));

		all.add(C(MessageAnalyzerManager.class, DefaultMessageAnalyzerManager.class));

		all.add(C(TcpSocketReceiver.class).req(ServerConfigManager.class).req(ServerStatisticManager.class)
		      .req(MessageCodec.class, PlainTextMessageCodec.ID).req(MessageHandler.class));

		all.add(C(MessageHandler.class, DefaultMessageHandler.class));

		all.add(C(AggregationHandler.class, DefaultAggregationHandler.class));

		all.add(C(AggregationConfigManager.class).req(AggregationHandler.class, ConfigDao.class));

		all.add(C(AppConfigManager.class).req(ConfigDao.class));

		all.add(C(AppDataService.class).req(AppConfigManager.class, AppDataCommandDao.class));

		all.add(C(UrlPatternHandler.class, DefaultUrlPatternHandler.class));

		all.add(C(UrlPatternConfigManager.class).req(ConfigDao.class, UrlPatternHandler.class));

		all.add(C(MessageBucket.class, LocalMessageBucket.ID, LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class, ServerStatisticManager.class));

		all.add(C(Module.class, CatCoreModule.ID, CatCoreModule.class));

		all.addAll(new CatCoreDatabaseConfigurator().defineComponents());
		all.addAll(new CodecComponentConfigurator().defineComponents());
		all.addAll(new StorageComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
