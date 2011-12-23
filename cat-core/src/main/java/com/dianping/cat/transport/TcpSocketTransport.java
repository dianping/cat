package com.dianping.cat.transport;

import com.dianping.cat.message.Message;

public class TcpSocketTransport implements Transport {
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
