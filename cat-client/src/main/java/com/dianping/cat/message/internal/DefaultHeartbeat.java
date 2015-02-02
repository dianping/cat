package com.dianping.cat.message.internal;

import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultHeartbeat extends AbstractMessage implements Heartbeat {
	private MessageManager m_manager;

	public DefaultHeartbeat(String type, String name) {
		super(type, name);
	}

	public DefaultHeartbeat(String type, String name, MessageManager manager) {
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
