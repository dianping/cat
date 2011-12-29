package com.dianping.cat.message.spi;

public interface MessageQueue {
	public boolean isActive();

	public MessageTree poll();
}
