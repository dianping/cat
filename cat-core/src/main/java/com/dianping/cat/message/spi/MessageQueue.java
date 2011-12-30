package com.dianping.cat.message.spi;

public interface MessageQueue {
	public boolean isActive();

	public int size();

	public MessageTree poll();

	public void offer(MessageTree tree);
}
