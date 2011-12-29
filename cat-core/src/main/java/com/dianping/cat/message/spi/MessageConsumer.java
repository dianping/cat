package com.dianping.cat.message.spi;

public interface MessageConsumer {
	public String getConsumerId();

	public String getDomain();

	public void consume(MessageTree tree);
}
