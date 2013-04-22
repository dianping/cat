package com.dianping.cat.message.spi;

public interface MessageConsumer {
	public void consume(MessageTree tree);
}
