package com.dianping.cat.consumer.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public class DefaultMessageQueue implements MessageQueue {
	private BlockingQueue<MessageTree> queue = new LinkedBlockingQueue<MessageTree>();

	@Override
	public MessageTree poll() {
		try {
			return queue.poll(1, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
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
