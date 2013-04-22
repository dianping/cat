package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;

public class DummyConsumer implements MessageConsumer {
	public static final String ID = "dummy";

	@Override
	public void consume(MessageTree tree) {
		// Do nothing here
	}
}
