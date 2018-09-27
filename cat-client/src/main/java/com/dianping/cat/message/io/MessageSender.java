package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.List;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageSender {
	public void initialize(List<InetSocketAddress> addresses);

	public void send(MessageTree tree);

	public void shutdown();
}
