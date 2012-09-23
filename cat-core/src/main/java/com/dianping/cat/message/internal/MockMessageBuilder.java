package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class MockMessageBuilder {
	private Stack<TransactionHolder> m_stack = new Stack<TransactionHolder>();

	public final Message build() {
		try {
			return define().build();
		} finally {
			m_stack.clear();
		}
	}

	public abstract MessageHolder define();

	protected EventHolder e(String type, String name) {
		EventHolder e = new EventHolder(type, name);

		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		return e;
	}

	protected HeartbeatHolder h(String type, String name) {
		HeartbeatHolder h = new HeartbeatHolder(type, name);

		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			h.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		return h;
	}

	protected TransactionHolder t(String type, String name, long durationInMillis) {
		TransactionHolder t = new TransactionHolder(type, name, durationInMillis);
		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			t.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		m_stack.push(t);
		return t;
	}

	protected static abstract class AbstractMessageHolder implements MessageHolder {
		private String m_type;

		private String m_name;

		private long m_timestampInMicros;

		private String m_status = "0";

		public AbstractMessageHolder(String type, String name) {
			m_type = type;
			m_name = name;
		}

		public String getName() {
			return m_name;
		}

		public String getStatus() {
			return m_status;
		}

		@Override
		public long getTimestampInMicros() {
			return m_timestampInMicros;
		}

		public long getTimestampInMillis() {
			return m_timestampInMicros / 1000;
		}

		public String getType() {
			return m_type;
		}

		public void setStatus(String status) {
			m_status = status;
		}

		@Override
		public void setTimestampInMicros(long timestampInMicros) {
			m_timestampInMicros = timestampInMicros;
		}
	}

	protected static class EventHolder extends AbstractMessageHolder {
		private DefaultEvent m_event;

		public EventHolder(String type, String name) {
			super(type, name);
		}

		@Override
		public Event build() {
			m_event = new DefaultEvent(getType(), getName());
			m_event.setTimestamp(getTimestampInMillis());
			m_event.setStatus(getStatus());
			m_event.complete();
			return m_event;
		}

		public EventHolder status(String status) {
			setStatus(status);
			return this;
		}
	}

	protected static class HeartbeatHolder extends AbstractMessageHolder {
		private DefaultHeartbeat m_heartbeat;

		public HeartbeatHolder(String type, String name) {
			super(type, name);
		}

		@Override
		public Heartbeat build() {
			m_heartbeat = new DefaultHeartbeat(getType(), getName());
			m_heartbeat.setTimestamp(getTimestampInMillis());
			m_heartbeat.setStatus(getStatus());
			m_heartbeat.complete();
			return m_heartbeat;
		}

		public HeartbeatHolder status(String status) {
			setStatus(status);
			return this;
		}
	}

	protected static interface MessageHolder {
		public Message build();

		public long getTimestampInMicros();

		public void setTimestampInMicros(long timestampInMicros);
	}

	protected class TransactionHolder extends AbstractMessageHolder {
		private long m_durationInMicros;

		private long m_currentTimestampInMicros;

		private List<MessageHolder> m_children = new ArrayList<MessageHolder>();

		private DefaultTransaction m_transaction;

		private long m_markTimestampInMicros;

		public TransactionHolder(String type, String name, long durationInMicros) {
			super(type, name);

			m_durationInMicros = durationInMicros;
		}

		public TransactionHolder after(long periodInMicros) {
			m_currentTimestampInMicros += periodInMicros;
			return this;
		}

		public TransactionHolder at(long timestampInMillis) {
			m_currentTimestampInMicros = timestampInMillis * 1000;
			setTimestampInMicros(m_currentTimestampInMicros);
			return this;
		}

		@Override
		public Transaction build() {
			m_transaction = new DefaultTransaction(getType(), getName(), null);
			m_transaction.setTimestamp(getTimestampInMillis());

			for (MessageHolder child : m_children) {
				m_transaction.addChild(child.build());
			}

			m_transaction.setStatus(getStatus());
			m_transaction.complete();
			m_transaction.setDurationInMicros(m_durationInMicros);
			return m_transaction;
		}

		public TransactionHolder child(MessageHolder child) {
			if (child instanceof TransactionHolder) {
				m_currentTimestampInMicros += ((TransactionHolder) child).getDurationInMicros();
				m_stack.pop();
			}

			m_children.add(child);
			return this;
		}

		public long getCurrentTimestampInMicros() {
			return m_currentTimestampInMicros;
		}

		public long getDurationInMicros() {
			return m_durationInMicros;
		}

		public TransactionHolder mark() {
			m_markTimestampInMicros = m_currentTimestampInMicros;
			return this;
		}

		public TransactionHolder reset() {
			m_currentTimestampInMicros = m_markTimestampInMicros;
			return this;
		}

		@Override
		public void setTimestampInMicros(long timestampInMicros) {
			super.setTimestampInMicros(timestampInMicros);

			m_currentTimestampInMicros = timestampInMicros;
		}

		public TransactionHolder status(String status) {
			setStatus(status);
			return this;
		}
	}
}
