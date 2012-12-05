package com.dianping.cat.message.spi;

public interface MessageQueue {

	// the current size of the queue
	public int size();

	public MessageTree poll();

	public boolean offer(MessageTree tree);
}
