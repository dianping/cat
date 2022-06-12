package com.dianping.cat.message.internal;

import com.dianping.cat.message.BulkEvent;
import com.dianping.cat.message.context.MessageContext;

public class DefaultBulkEvent extends DefaultEvent implements BulkEvent {
	private int m_success;

	private int m_failed;

	public DefaultBulkEvent(MessageContext ctx, String type, String name) {
		super(ctx, type, name);
	}

	@Override
	public DefaultBulkEvent addCount(int success, int failed) {
		m_success = success;
		m_failed += failed;
		return this;
	}

	@Override
	public CharSequence getData() {
		if (m_success + m_failed > 0) {
			super.addData("@", m_success + "," + m_failed);
		}

		return super.getData();
	}

}
