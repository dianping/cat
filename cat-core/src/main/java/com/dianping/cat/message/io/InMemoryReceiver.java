package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class InMemoryReceiver implements MessageReceiver {
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
	public void onMessage(MessageHandler handler) {
		try {
			while (true) {
				MessageTree tree = m_queue.poll(1);

				if (tree != null) {
					handler.handle(tree);
				} else if (!isActive()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			// ignore it
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
