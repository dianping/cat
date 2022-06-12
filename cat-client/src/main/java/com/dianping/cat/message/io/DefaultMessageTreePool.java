package com.dianping.cat.message.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.message.tree.MessageTree;

// Component
public class DefaultMessageTreePool implements MessageTreePool, Initializable, LogEnabled {
	// Inject
	private ConfigureManager m_configureManager;

	private BlockingQueue<MessageTree> m_queue;

	private AtomicInteger m_errors = new AtomicInteger();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void feed(MessageTree tree) {
		boolean blocked = m_configureManager.getBooleanProperty(ConfigureProperty.BLOCKED, false);

		if (blocked) {
			// TODO blocked but keep heart-beat message
			// so we know how many messages blocked
		} else {
			boolean result = m_queue.offer(tree);

			if (!result) {
				logQueueFullInfo();
			}
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);

		int size = m_configureManager.getIntProperty(ConfigureProperty.SENDER_MESSAGE_QUEUE_SIZE, 5000);

		m_queue = new ArrayBlockingQueue<MessageTree>(size);
	}

	private void logQueueFullInfo() {
		int count = m_errors.incrementAndGet();

		if (count % 1000 == 0 || count == 1) {
			m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
		}
	}

	@Override
	public MessageTree poll() throws InterruptedException {
		return m_queue.poll(5, TimeUnit.MILLISECONDS);
	}

	@Override
	public int size() {
		return m_queue.size();
	}
}
