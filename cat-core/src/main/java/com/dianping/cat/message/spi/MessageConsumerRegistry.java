package com.dianping.cat.message.spi;

import java.util.List;

public interface MessageConsumerRegistry {
	public void registerConsumer(MessageConsumer consumer);

	public List<MessageConsumer> getConsumers();
}