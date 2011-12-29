package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private long m_duration;

	private List<Message> m_children;

	public DefaultTransaction(String type, String name) {
		super(type, name);

		MessageManager.INSTANCE.start(this);
	}

	@Override
	public void addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		m_children.add(message);
	}

	@Override
	public void complete() {
		if (isCompleted()) {
			// complete() was called more than once
			DefaultEvent event = new DefaultEvent("CAT", "BadInstrument");

			event.setStatus("TransactionAlreadyCompleted");
			event.complete();
		} else {
			m_duration = (long) (System.nanoTime() / 1e6) - getTimestamp();

			setCompleted(true);
			MessageManager.INSTANCE.end(this);
		}
	}

	@Override
	public List<Message> getChildren() {
		if (m_children == null) {
			return Collections.emptyList();
		}

		return m_children;
	}

	@Override
	public long getDuration() {
		return m_duration;
	}
}
