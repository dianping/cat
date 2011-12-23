package com.dianping.cat.message.transport;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;

public class UdpMulticastTransport implements Transport {
	@Override
	public void send(Message message) {
	}

	@Override
	public void onMessage(MessageHandler handler) {
	}

	@Override
	public void shutdown() {
	}
}
