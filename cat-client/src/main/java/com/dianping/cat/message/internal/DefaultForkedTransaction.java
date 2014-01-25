package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultForkedTransaction extends AbstractMessage implements Transaction, ForkedTransaction {
	private long m_durationInMicro = -1; // must be less than 0

	private List<Message> m_children;

	private MessageManager m_manager;

	private boolean m_standalone;

	private long m_durationStart;

	private String m_forkedMessageId;

	public DefaultForkedTransaction(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
		m_standalone = false;
		m_durationStart = System.nanoTime();
	}

	@Override
	public DefaultForkedTransaction addChild(Message message) {
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
			m_durationInMicro = (System.nanoTime() - m_durationStart) / 1000L;

			setCompleted(true);

			if (m_manager != null) {
				m_manager.end(this);
			}
		}
	}

	@Override
	public void fork() {
		m_manager.setup();
		m_manager.start(this, false);

		MessageTree tree = m_manager.getThreadLocalMessageTree();

		if (tree != null) {
			m_forkedMessageId = tree.getMessageId();
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
					DefaultForkedTransaction trx = (DefaultForkedTransaction) lastChild;

					duration = (trx.getTimestamp() - getTimestamp()) * 1000L + trx.getDurationInMicros();
				} else {
					duration = (lastChild.getTimestamp() - getTimestamp()) * 1000L;
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
	public String getForkedMessageId() {
		return m_forkedMessageId;
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
