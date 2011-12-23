package com.dianping.cat.transport;

import com.dianping.cat.message.Message;

public interface Transport {
	public void onMessage(MessageHandler handler);

	public void send(Message message);

	public void shutdown();
}
