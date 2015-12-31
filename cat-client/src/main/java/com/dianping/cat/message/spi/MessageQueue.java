package com.dianping.cat.message.spi;

public interface MessageQueue {
	public boolean offer(MessageTree tree);

	public boolean offer(MessageTree tree, double sampleRatio);

	public MessageTree peek();

	public MessageTree poll();

	// the current size of the queue
	public int size();
}
