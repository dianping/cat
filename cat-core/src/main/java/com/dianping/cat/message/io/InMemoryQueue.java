package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.Message;

public class InMemoryQueue implements LogEnabled, Initializable {
	private BlockingQueue<Message> m_queue;

	private int m_queueSize;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_queueSize <= 0) {
			m_queue = new LinkedBlockingQueue<Message>();
		} else {
			m_queue = new LinkedBlockingQueue<Message>(m_queueSize);
		}
	}

	public void offer(Message message) {
		while (!m_queue.offer(message)) {
			// throw away the message at the tail
			Message m = m_queue.poll();

			if (m == null) {
				break;
			} else {
				m_logger.warn(message + " was thrown away due to queue is full!");
			}
		}
	}

	public Message poll(long timeout) throws InterruptedException {
		return m_queue.poll(timeout, TimeUnit.MILLISECONDS);
	}

	public void setQueueSize(int queueSize) {
		m_queueSize = queueSize;
	}
}
