package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.consumer.core.dal.DailyReportDao;
import com.dianping.cat.consumer.core.dal.MonthlyReportDao;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.WeeklyReportDao;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.core.DefaultMessageHandler;
import com.dianping.cat.message.spi.core.DefaultMessagePathBuilder;
import com.dianping.cat.message.spi.core.MessageHandler;
import com.dianping.cat.message.spi.core.MessagePathBuilder;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.service.DefaultReportService;
import com.dianping.cat.service.RemoteModelService;
import com.dianping.cat.service.ReportService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.storage.dump.ChannelBufferManager;
import com.dianping.cat.storage.dump.LocalMessageBucket;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucket;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ServerConfigManager.class));
		all.add(C(ServerStatisticManager.class));

		all.add(C(MessagePathBuilder.class, DefaultMessagePathBuilder.class) //
		      .req(ClientConfigManager.class));

		all.add(C(MessageAnalyzerManager.class, DefaultMessageAnalyzerManager.class));

		all.add(C(RemoteModelService.class));
		all.add(C(ReportService.class, DefaultReportService.class) //
		      .req(ServerConfigManager.class, RemoteModelService.class) //
		      .req(ReportDao.class, DailyReportDao.class, WeeklyReportDao.class, MonthlyReportDao.class));

		all.add(C(TcpSocketReceiver.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID)//
		      .req(ServerConfigManager.class, MessageHandler.class)//
		      .req(ServerStatisticManager.class));

		all.add(C(MessageHandler.class, DefaultMessageHandler.class));

		all.add(C(MessageBucket.class, LocalMessageBucket.ID, LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class, ServerStatisticManager.class));
		all.add(C(ChannelBufferManager.class));

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
