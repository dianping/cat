package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageTree;

public class InMemoryQueue implements LogEnabled, Initializable {
	private BlockingQueue<MessageTree> m_queue;

	private int m_queueSize;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_queueSize <= 0) {
			m_queue = new LinkedBlockingQueue<MessageTree>();
		} else {
			m_queue = new LinkedBlockingQueue<MessageTree>(m_queueSize);
		}
	}

	public void offer(MessageTree tree) {
		while (!m_queue.offer(tree)) {
			// throw away the tree at the tail
			MessageTree m = m_queue.poll();

			if (m == null) {
				break;
			} else {
				m_logger.warn(tree + " was thrown away due to queue is full!");
			}
		}
	}

	public MessageTree peek() {
		return m_queue.peek();
	}

	public MessageTree poll(long timeout) throws InterruptedException {
		if (timeout <= 0) {
			return m_queue.poll();
		} else {
			return m_queue.poll(timeout, TimeUnit.MILLISECONDS);
		}
	}

	public void setQueueSize(int queueSize) {
		m_queueSize = queueSize;
	}

	public int size() {
		return m_queue.size();
	}

	public void clear() {
		m_queue.clear();
	}
}
