package com.dianping.cat.message.consumer.impl;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageQueue implements MessageQueue {
	
	private Queue<MessageTree> queue = new LinkedBlockingQueue<MessageTree>();
	
	@Override
	public MessageTree poll() {
		return queue.poll();
	}

	@Override
	public void offer(MessageTree tree) {
		queue.add(tree);
	}

	@Override
	public int size() {
		return queue.size();
	}

}
