package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.MessageManager;
import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.io.InMemoryReceiver;
import com.dianping.cat.message.io.InMemorySender;
import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.consumer.DummyConsumer;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		String inMemory = "in-memory";

		all.add(C(InMemoryQueue.class));
		all.add(C(MessageSender.class, inMemory, InMemorySender.class) //
		      .req(InMemoryQueue.class));
		all.add(C(MessageReceiver.class, inMemory, InMemoryReceiver.class) //
		      .req(InMemoryQueue.class));

		all.add(C(MessageManager.class) //
		      .req(MessageSender.class, inMemory));
		all.add(C(MessageProducer.class, DefaultMessageProducer.class) //
		      .req(MessageManager.class));

		all.add(C(MessageCodec.class, "plain-text", PlainTextMessageCodec.class));

		all.add(C(MessageConsumer.class, "dummy", DummyConsumer.class));
		all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
		      .req(MessageConsumer.class, new String[] { "dummy" }, "m_consumers"));

		// the following are not used right now
		all.add(C(MessageHandler.class, DefaultMessageHandler.class) //
		      .req(MessageConsumerRegistry.class) //
		      .req(MessageReceiver.class, inMemory));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
