package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageSender {
	public void initialize();

	public void send(MessageTree tree);

	public void shutdown();
}
