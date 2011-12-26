package com.dianping.cat.message.io;

import com.dianping.cat.message.Message;

public interface MessageSender {
	public void initialize();

	public void send(Message message);

	public void shutdown();
}
