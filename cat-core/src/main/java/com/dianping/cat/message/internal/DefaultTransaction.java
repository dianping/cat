package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private long m_durationInMicro = -1; // must be less than 0

	private List<Message> m_children;

	private MessageManager m_manager;

	private boolean m_standalone;

	public DefaultTransaction(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
		m_standalone = true;
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
			m_durationInMicro = MilliSecondTimer.currentTimeMicros() - getTimestampInMicros();

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
	public long getDurationInMicros() {
		if (m_durationInMicro >= 0) {
			return m_durationInMicro;
		} else { // if it's not completed explicitly
			long duration = 0;
			int len = m_children == null ? 0 : m_children.size();

			if (len > 0) {
				Message lastChild = m_children.get(len - 1);

				if (lastChild instanceof Transaction) {
					DefaultTransaction trx = (DefaultTransaction) lastChild;

					duration = trx.getTimestampInMicros() + trx.getDurationInMicros() - getTimestampInMicros();
				} else {
					duration = lastChild.getTimestamp() * 1000L - getTimestampInMicros();
				}
			}

			return duration;
		}
	}

	@Override
	public long getDurationInMillis() {
		return getDurationInMicros() / 1000L;
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	@Override
	public boolean isStandalone() {
		return m_standalone;
	}

	public void setDurationInMicros(long duration) {
		m_durationInMicro = duration;
	}

	public void setDurationInMillis(long duration) {
		m_durationInMicro = duration * 1000L;
	}

	public void setStandalone(boolean standalone) {
		m_standalone = standalone;
	}
}
