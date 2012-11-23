package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.lookup.annotation.Inject;

public class DefaultMessageQueue implements MessageQueue, Initializable {
	private BlockingQueue<MessageTree> m_queue;

	@Inject
	private int m_size;

	@Override
	public void initialize() throws InitializationException {
		if (m_size > 0) {
			m_queue = new LinkedBlockingQueue<MessageTree>(m_size);
		} else {
			m_queue = new LinkedBlockingQueue<MessageTree>(10000);
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
