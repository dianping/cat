package com.dianping.cat.message.handler;

import java.util.List;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.consumer.MessageConsumer;
import com.dianping.cat.message.consumer.MessageConsumerRegistry;
import com.dianping.cat.message.io.MessageReceiver;
import com.site.lookup.annotation.Inject;

public class MessageDispatcher implements MessageHandler, Runnable {
	@Inject
	private MessageReceiver m_receiver;

	@Inject
	private MessageConsumerRegistry m_registry;

	@Override
	public void handle(Message message) {
		List<MessageConsumer> comsumers = m_registry.getConsumers();

		for (MessageConsumer consumer : comsumers) {
			try {
				consumer.consume(message);
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
