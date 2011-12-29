package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class InMemorySender implements MessageSender {
	@Inject
	private InMemoryQueue m_queue;

	private transient boolean m_active = true;

	@Override
	public void initialize() {
	}

	public boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	@Override
	public void send(MessageTree tree) {
		if (isActive()) {
			m_queue.offer(tree);
		}
	}

	public void setQueue(InMemoryQueue queue) {
		m_queue = queue;
	}

	@Override
	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}
}
