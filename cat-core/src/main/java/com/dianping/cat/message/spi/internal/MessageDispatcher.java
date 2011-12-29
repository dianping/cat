package com.dianping.cat.message.spi.internal;

import java.util.Collection;

import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class MessageDispatcher implements MessageHandler, Runnable {
	@Inject
	private MessageReceiver m_receiver;

	@Inject
	private DefaultMessageConsumerRegistry m_registry;

	@Override
	public void handle(MessageTree tree) {
		Collection<MessageConsumer> comsumers = m_registry.getConsumers().values();

		for (MessageConsumer consumer : comsumers) {
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

	public void setRegistry(DefaultMessageConsumerRegistry registry) {
		m_registry = registry;
	}
}
