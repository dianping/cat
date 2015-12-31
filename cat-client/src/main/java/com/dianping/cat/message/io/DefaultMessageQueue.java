package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageQueue implements MessageQueue {
	private BlockingQueue<MessageTree> m_queue;

	private AtomicInteger m_count = new AtomicInteger();

	public DefaultMessageQueue(int size) {
		m_queue = new LinkedBlockingQueue<MessageTree>(size);
	}

	@Override
	public boolean offer(MessageTree tree) {
		return m_queue.offer(tree);
	}

	@Override
	public boolean offer(MessageTree tree, double sampleRatio) {
		if (tree.isSample() && sampleRatio < 1.0) {
			if (sampleRatio > 0) {
				int count = m_count.incrementAndGet();

				if (count % (1 / sampleRatio) == 0) {
					return offer(tree);
				}
			}
			return false;
		} else {
			return offer(tree);
		}
	}

	@Override
	public MessageTree peek() {
		return m_queue.peek();
	}

	@Override
	public MessageTree poll() {
		try {
			return m_queue.poll(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public int size() {
		return m_queue.size();
	}
}
