package com.dianping.cat.message.spi;

import java.util.Map;

public interface MessageConsumerRegistry {
	public void registerFilter(MessageFilter filter);

	public void registerConsumer(MessageConsumer consumer);

	public Map<String, MessageConsumer> getConsumers();
}