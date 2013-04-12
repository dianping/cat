package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageQueue implements MessageQueue {
	private BlockingQueue<MessageTree> m_queue;

	@Inject
	private int m_size;

	private static final int SIZE = 500000;

	public DefaultMessageQueue() {
		this(SIZE);
	}

	public DefaultMessageQueue(int size) {
		if (size > 0) {
			m_queue = new LinkedBlockingQueue<MessageTree>(size);
		} else {
			m_queue = new LinkedBlockingQueue<MessageTree>(SIZE);
		}
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
	public boolean offer(MessageTree tree) {
		return m_queue.offer(tree);
	}

	@Override
	public int size() {
		return m_queue.size();
	}

	public void setSize(int size) {
		m_size = size;
	}
}
