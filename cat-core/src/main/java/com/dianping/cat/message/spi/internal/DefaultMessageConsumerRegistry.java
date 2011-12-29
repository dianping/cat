package com.dianping.cat.message.spi.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageFilter;

public class DefaultMessageConsumerRegistry implements MessageConsumerRegistry {
	private Map<String, MessageConsumer> m_consumers = new LinkedHashMap<String, MessageConsumer>();

	@Override
	public Map<String, MessageConsumer> getConsumers() {
		return m_consumers;
	}

	@Override
	public void registerConsumer(MessageConsumer consumer) {
		m_consumers.put(consumer.getId(), consumer);
	}

	@Override
	public void registerFilter(MessageFilter filter) {

	}

	static class Entry {
		private MessageConsumer m_message;

		private List<MessageFilter> m_filters;
	}
}
