package com.dianping.cat.message.spi.consumer;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;

public class DummyConsumer implements MessageConsumer {
	@Override
	public String getConsumerId() {
		return "dummy";
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public void consume(MessageTree tree) {
		// Do nothing here
	}
}
