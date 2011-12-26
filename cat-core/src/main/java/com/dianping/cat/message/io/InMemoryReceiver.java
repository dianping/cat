package com.dianping.cat.message.io;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;
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
				Message m = m_queue.poll(1);

				if (m != null) {
					handler.handle(m);
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
