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

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
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

	protected MetricHolder m(String type, String name) {
		MetricHolder e = new MetricHolder(type, name);

		TransactionHolder parent = m_stack.isEmpty() ? null : m_stack.peek();

		if (parent != null) {
			e.setTimestampInMicros(parent.getCurrentTimestampInMicros());
		}

		return e;
	}

	protected MetricHolder m(String type, String name, String data) {
		MetricHolder e = new MetricHolder(type, name, data);

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
			m_event = new DefaultEvent(getType(), getName(), null);
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

	protected static class MetricHolder extends AbstractMessageHolder {
		private DefaultMetric m_metric;

		public MetricHolder(String type, String name) {
			super(type, name);
		}

		public MetricHolder(String type, String name, String data) {
			super(type, name, data);

		}

		@Override
		public Metric build() {
			m_metric = new DefaultMetric(getType(), getName());
			m_metric.setTimestamp(getTimestampInMillis());
			m_metric.setStatus(getStatus());
			m_metric.addData(getData());
			m_metric.complete();
			return m_metric;
		}

		public MetricHolder status(String status) {
			setStatus(status);
			return this;
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
			m_transaction = new DefaultTransaction(getType(), getName(), null);
			m_transaction.setTimestamp(getTimestampInMillis());

			for (MessageHolder child : m_children) {
				m_transaction.addChild(child.build());
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
