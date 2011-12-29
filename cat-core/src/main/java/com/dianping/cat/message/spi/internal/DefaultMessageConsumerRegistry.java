package com.dianping.cat.message.spi.internal;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;

public class DefaultMessageConsumerRegistry implements MessageConsumerRegistry {
	private List<MessageConsumer> m_consumers = new ArrayList<MessageConsumer>();

	@Override
	public List<MessageConsumer> getConsumers() {
		return m_consumers;
	}

	@Override
	public void registerConsumer(MessageConsumer consumer) {
		m_consumers.add(consumer);
	}
}
