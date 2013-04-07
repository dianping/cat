package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.io.DefaultTransportManager;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TcpSocketHierarchySender;
import com.dianping.cat.message.io.TcpSocketReceiver;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.codec.HtmlMessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.consumer.DummyConsumer;
import com.dianping.cat.message.spi.consumer.DumpToHtmlConsumer;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.dianping.cat.message.spi.internal.DefaultMessagePathBuilder;
import com.dianping.cat.message.spi.internal.DefaultMessageStatistics;
import com.dianping.cat.message.spi.internal.DefaultMessageStorage;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.status.StatusUpdateTask;
import com.dianping.cat.storage.dump.ChannelBufferManager;
import com.dianping.cat.storage.dump.LocalMessageBucket;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucket;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ClientConfigManager.class));
		all.add(C(ServerConfigManager.class));
		all.add(C(ServerStateManager.class));

		all.add(C(MessageManager.class, DefaultMessageManager.class) //
		      .req(ClientConfigManager.class, TransportManager.class, MessageStatistics.class));
		all.add(C(MessageProducer.class, DefaultMessageProducer.class) //
		      .req(MessageManager.class, MessageIdFactory.class));
		all.add(C(MessageIdFactory.class));
		all.add(C(MessagePathBuilder.class, DefaultMessagePathBuilder.class) //
		      .req(ClientConfigManager.class));

		all.add(C(MessageStorage.class, "html", DefaultMessageStorage.class) //
		      .req(MessagePathBuilder.class) //
		      .req(MessageCodec.class, HtmlMessageCodec.ID));
		all.add(C(MessageConsumer.class, DummyConsumer.ID, DummyConsumer.class));
		all.add(C(MessageConsumer.class, DumpToHtmlConsumer.ID, DumpToHtmlConsumer.class) //
		      .req(MessageStorage.class, HtmlMessageCodec.ID) //
		      .req(MessagePathBuilder.class));
		all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
		      .req(MessageConsumer.class, new String[] { DummyConsumer.ID }, "m_consumers"));

		all.add(C(MessageQueue.class, DefaultMessageQueue.class) //
		      .config(E("size").value("10000")) //
		      .is(PER_LOOKUP));

		all.add(C(MessageSender.class, "tcp-socket", TcpSocketSender.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageStatistics.class, "default", "m_statistics") //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID, "m_codec")//
		      .req(MessageQueue.class, "default", "m_queue"));
		all.add(C(MessageSender.class, "tcp-socket-hierarchy", TcpSocketHierarchySender.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageStatistics.class, "default", "m_statistics") //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID, "m_codec")//
		      .req(MessageQueue.class, "default", "m_queue"));
		all.add(C(TcpSocketReceiver.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID)//
		      .req(ServerConfigManager.class, MessageHandler.class)//
		      .req(ServerStateManager.class));
		all.add(C(TransportManager.class, DefaultTransportManager.class) //
		      .req(ClientConfigManager.class));

		all.add(C(MessageHandler.class, DefaultMessageHandler.class));
		all.add(C(MessageStatistics.class, DefaultMessageStatistics.class));
		all.add(C(StatusUpdateTask.class) //
		      .req(MessageStatistics.class, ClientConfigManager.class));

		all.add(C(MessageBucket.class, LocalMessageBucket.ID, LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID) //
		      .req(ChannelBufferManager.class));
		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class, ServerStateManager.class));
		all.add(C(ChannelBufferManager.class));

		all.add(C(Module.class, CatCoreModule.ID, CatCoreModule.class));

		all.addAll(new CodecComponentConfigurator().defineComponents());
		all.addAll(new StorageComponentConfigurator().defineComponents());
		all.addAll(new ABTestComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
