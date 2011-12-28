package com.dianping.cat.message.internal;

import com.dianping.cat.message.Heartbeat;

public class DefaultHeartbeat extends AbstractMessage implements Heartbeat {
	public DefaultHeartbeat(String type, String name) {
		super(type, name);
	}

	@Override
	public void complete() {
		setCompleted(true);
		MessageManager.INSTANCE.add(this);
	}
}
