package com.dianping.cat.message.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageQueue extends ContainerHolder implements MessageQueue, Initializable {
	private BlockingQueue<MessageTree> m_queue;

	@Inject
	private int m_size;

	@Override
	public void destroy() {
		super.release(this);
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_size > 0) {
			m_queue = new LinkedBlockingQueue<MessageTree>(m_size);
		} else {
			m_queue = new LinkedBlockingQueue<MessageTree>(50000);
		}
	}

	@Override
	public boolean offer(MessageTree tree) {
		return m_queue.offer(tree);
	}

	@Override
	public MessageTree poll() {
		try {
			return m_queue.poll(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	public void setSize(int size) {
		m_size = size;
	}

	@Override
	public int size() {
		return m_queue.size();
	}
}
