package com.dianping.cat.message.consumer;

import java.util.ArrayList;
import java.util.List;

public class MessageConsumerRegistry {
	private List<MessageConsumer> m_consumers = new ArrayList<MessageConsumer>();

	public List<MessageConsumer> getConsumers() {
		return m_consumers;
	}
	
	public void register(MessageConsumer consumer) {
		m_consumers.add(consumer);
	}
}
