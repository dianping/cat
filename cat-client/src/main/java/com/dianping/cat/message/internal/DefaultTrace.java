package com.dianping.cat.message.internal;

import com.dianping.cat.message.Trace;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultTrace extends AbstractMessage implements Trace {
	private MessageManager m_manager;

	public DefaultTrace(String type, String name) {
		super(type, name);
	}

	public DefaultTrace(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
	}

	@Override
	public void complete() {
		setCompleted(true);

		if (m_manager != null) {
			m_manager.add(this);
		}
	}
}
