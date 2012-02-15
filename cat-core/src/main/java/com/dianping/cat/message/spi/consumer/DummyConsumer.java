package com.dianping.cat.message.spi.consumer;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;

public class DummyConsumer implements MessageConsumer {
	public static final String ID = "dummy";

	@Override
	public String getConsumerId() {
		return ID;
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
