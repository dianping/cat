package com.dianping.cat.transport;

import com.dianping.cat.message.Message;

public interface MessageHandler {
	public void handle(Message message);
}
