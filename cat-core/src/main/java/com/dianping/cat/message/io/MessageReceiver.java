package com.dianping.cat.message.io;

import com.dianping.cat.message.handler.MessageHandler;

public interface MessageReceiver {
	public void initialize();

	public void onMessage(MessageHandler handler);

	public void shutdown();
}
