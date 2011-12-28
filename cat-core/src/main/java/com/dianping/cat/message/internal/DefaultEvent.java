package com.dianping.cat.message.internal;

import com.dianping.cat.message.Event;

public class DefaultEvent extends AbstractMessage implements Event {
	public DefaultEvent(String type, String name) {
		super(type, name);
	}

	@Override
	public void complete() {
		setCompleted(true);
		MessageManager.INSTANCE.add(this);
	}
}
