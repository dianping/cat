/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.MetricContextHelper;

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

	protected EventHolder e(String type, String name, String data) {
		EventHolder e = new EventHolder(type, name, data);

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

	protected MetricHolder m(String name) {
		MetricHolder e = new MetricHolder(name);

		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		return e;
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

	protected TransactionHolder t(String type, String name, String data, long durationInMillis) {
		TransactionHolder t = new TransactionHolder(type, name, data, durationInMillis);
		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			t.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		m_stack.push(t);
		return t;
	}

	protected static interface MessageHolder {
		public Message build();

		public long getTimestampInMicros();

		public void setTimestampInMicros(long timestampInMicros);
	}

	protected static abstract class AbstractMessageHolder implements MessageHolder {
		private String m_type;

		private String m_name;

		private String m_data;

		private long m_timestampInMicros;

		private String m_status = "0";

		public AbstractMessageHolder(String type, String name) {
			m_type = type;
			m_name = name;
		}

		public AbstractMessageHolder(String type, String name, String data) {
			m_type = type;
			m_name = name;
			m_data = data;
		}

		public void addData(String key, String value) {
			if (m_data == null) {
				m_data = key + "=" + value;
			} else {
				m_data = m_data + "&" + key + "=" + value;
			}
		}

		public String getData() {
			return m_data;
		}

		public String getName() {
			return m_name;
		}

		public String getStatus() {
			return m_status;
		}

		public void setStatus(String status) {
			m_status = status;
		}

		@Override
		public long getTimestampInMicros() {
			return m_timestampInMicros;
		}

		@Override
		public void setTimestampInMicros(long timestampInMicros) {
			m_timestampInMicros = timestampInMicros;
		}

		public long getTimestampInMillis() {
			return m_timestampInMicros / 1000;
		}

		public String getType() {
			return m_type;
		}
	}

	public static class EventHolder extends AbstractMessageHolder {
		private DefaultEvent m_event;

		public EventHolder(String type, String name) {
			super(type, name);
		}

		public EventHolder(String type, String name, String data) {
			super(type, name, data);

		}

		@Override
		public Event build() {
			m_event = (DefaultEvent) Cat.newEvent(getType(), getName());
			m_event.setTimestamp(getTimestampInMillis());
			m_event.setStatus(getStatus());
			m_event.addData(getData());
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
			m_heartbeat = (DefaultHeartbeat) Cat.newHeartbeat(getType(), getName());
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

	protected static class MetricHolder {
		private Metric m_metric;

		private long m_timestampInMicros;

		private String m_name;

		public MetricHolder(String name) {
			m_name = name;
		}

		public void setTimestampInMicros(long timestampInMicros) {
			m_timestampInMicros = timestampInMicros;
		}

		public Metric build() {
			m_metric = MetricContextHelper.context().newMetric(m_name);

			if (m_metric instanceof DefaultMetric) {
				((DefaultMetric) m_metric).setTimestamp(m_timestampInMicros % 1000L);
			}

			return m_metric;
		}
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

		public TransactionHolder(String type, String name, String data, long durationInMicros) {
			super(type, name, data);

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
			m_transaction = (DefaultTransaction) Cat.newTransaction(getType(), getName());
			m_transaction.setTimestamp(getTimestampInMillis());

			for (MessageHolder child : m_children) {
				child.build();
			}

			m_transaction.setStatus(getStatus());
			m_transaction.addData(getData());
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

		public TransactionHolder data(String key, String value) {
			addData(key, value);
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
