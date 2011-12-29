package com.dianping.cat.message.spi.internal;

import java.util.List;

import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultMessageHandler implements MessageHandler, Runnable {
	@Inject
	private MessageReceiver m_receiver;

	@Inject
	private MessageConsumerRegistry m_registry;

	@Override
	public void handle(MessageTree tree) {
		List<MessageConsumer> consumers = m_registry.getConsumers();
		int size = consumers.size();

		for (int i = 0; i < size; i++) {
			MessageConsumer consumer = consumers.get(i);

			try {
				consumer.consume(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		m_receiver.onMessage(this);
	}

	public void setReceiver(MessageReceiver receiver) {
		m_receiver = receiver;
	}

	public void setRegistry(MessageConsumerRegistry registry) {
		m_registry = registry;
	}
}
