package com.dianping.cat.message.spi;

public interface MessageConsumer {
	public String getConsumerId();

	public void consume(MessageTree tree);
}
