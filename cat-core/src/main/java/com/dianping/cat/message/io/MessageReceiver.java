package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageHandler;

public interface MessageReceiver {
	public void initialize();

	public void onMessage(MessageHandler handler);

	public void shutdown();
}
