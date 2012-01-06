package com.dianping.cat.message.io;

public interface TransportManager {
	public MessageReceiver getReceiver();

	public MessageSender getSender();
}
