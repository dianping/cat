package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private long m_duration = -1; // must be less than 0

	private List<Message> m_children;

	private MessageManager m_manager;

	public DefaultTransaction(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
	}

	@Override
	public DefaultTransaction addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		m_children.add(message);
		return this;
	}

	@Override
	public void complete() {
		if (isCompleted()) {
			// complete() was called more than once
			DefaultEvent event = new DefaultEvent("CAT", "BadInstrument");

			event.setStatus("TransactionAlreadyCompleted");
			event.complete();
			addChild(event);
		} else {
			m_duration = MilliSecondTimer.currentTimeMillis() - getTimestamp();

			setCompleted(true);

			if (m_manager != null) {
				m_manager.end(this);
			}
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
		if (m_duration >= 0) {
			return m_duration;
		} else { // if it's not completed explicitly
			long duration = 0;
			int len = m_children == null ? 0 : m_children.size();

			if (len > 0) {
				Message lastChild = m_children.get(len - 1);

				duration = lastChild.getTimestamp() - getTimestamp();

				if (lastChild instanceof Transaction) {
					duration += ((Transaction) lastChild).getDuration();
				}
			}

			return duration;
		}
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	public void setDuration(long duration) {
		m_duration = duration;
	}

}
