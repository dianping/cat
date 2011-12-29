package com.dianping.cat.message.spi;


public interface MessageConsumer {
	public String getId();

	public void consume(MessageTree tree);
}
