package com.dianping.cat.analysis;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageHandler {
	public void handle(MessageTree message);
}
