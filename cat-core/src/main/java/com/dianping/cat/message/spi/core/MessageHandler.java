package com.dianping.cat.message.spi.core;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageHandler {
	public void handle(MessageTree message);
}
