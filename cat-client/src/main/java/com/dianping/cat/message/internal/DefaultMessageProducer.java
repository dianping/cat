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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProducer.class)
public class DefaultMessageProducer implements MessageProducer {
	@Inject
	private MessageManager m_manager;

	@Inject
	private MessageIdFactory m_factory;

	@Override
	public String createRpcServerId(String domain) {
		return m_factory.getNextId(domain);
	}

	@Override
	public String createMessageId() {
		return m_factory.getNextId();
	}

	@Override
	public boolean isEnabled() {
		return m_manager.isMessageEnabled();
	}

	@Override
	public void logError(String message, Throwable cause) {
		if (Cat.getManager().isCatEnabled()) {
			if (shouldLog(cause)) {
				m_manager.getThreadLocalMessageTree().setDiscard(false);

				StringWriter writer = new StringWriter(2048);

				if (message != null) {
					writer.write(message);
					writer.write(' ');
				}

				cause.printStackTrace(new PrintWriter(writer));

				String detailMessage = writer.toString();

				if (cause instanceof Error) {
					logEvent("Error", cause.getClass().getName(), "ERROR", detailMessage);
				} else if (cause instanceof RuntimeException) {
					logEvent("RuntimeException", cause.getClass().getName(), "ERROR", detailMessage);
				} else {
					logEvent("Exception", cause.getClass().getName(), "ERROR", detailMessage);
				}
			}
		} else {
			cause.printStackTrace();
		}
	}

	@Override
	public void logError(Throwable cause) {
		logError(null, cause);
	}

	@Override
	public void logEvent(String type, String name) {
		logEvent(type, name, Message.SUCCESS, null);
	}

	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {
		Event event = newEvent(type, name);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			event.addData(nameValuePairs);
		}

		event.setStatus(status);
		event.complete();
	}

	@Override
	public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		Heartbeat heartbeat = newHeartbeat(type, name);

		heartbeat.addData(nameValuePairs);
		heartbeat.setStatus(status);
		heartbeat.complete();
	}

	@Override
	public void logMetric(String name, String status, String nameValuePairs) {
		String type = "";
		Metric metric = newMetric(type, name);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			metric.addData(nameValuePairs);
		}

		metric.setStatus(status);
		metric.complete();
	}

	@Override
	public void logTrace(String type, String name) {
		logTrace(type, name, Message.SUCCESS, null);
	}

	@Override
	public void logTrace(String type, String name, String status, String nameValuePairs) {
		if (m_manager.isTraceMode()) {
			Trace trace = newTrace(type, name);

			if (nameValuePairs != null && nameValuePairs.length() > 0) {
				trace.addData(nameValuePairs);
			}

			trace.setStatus(status);
			trace.complete();
		}
	}

	@Override
	public Event newEvent(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		return new DefaultEvent(type, name, m_manager);
	}

	public Event newEvent(Transaction parent, String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		DefaultEvent event = new DefaultEvent(type, name);

		parent.addChild(event);
		return event;
	}

	@Override
	public ForkedTransaction newForkedTransaction(String type, String name) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		MessageTree tree = m_manager.getThreadLocalMessageTree();

		if (tree.getMessageId() == null) {
			tree.setMessageId(createMessageId());
		}

		DefaultForkedTransaction transaction = new DefaultForkedTransaction(type, name, m_manager);

		if (m_manager instanceof DefaultMessageManager) {
			((DefaultMessageManager) m_manager).linkAsRunAway(transaction);
		}
		m_manager.start(transaction, true);
		return transaction;
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name, m_manager);

		m_manager.getThreadLocalMessageTree().setDiscard(false);
		return heartbeat;
	}

	@Override
	public Metric newMetric(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		DefaultMetric metric = new DefaultMetric(type == null ? "" : type, name, m_manager);

		m_manager.getThreadLocalMessageTree().setDiscard(false);
		return metric;
	}

	@Override
	public TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		MessageTree tree = m_manager.getThreadLocalMessageTree();

		if (tree.getMessageId() == null) {
			tree.setMessageId(createMessageId());
		}
		DefaultTaggedTransaction transaction = new DefaultTaggedTransaction(type, name, tag, m_manager);

		m_manager.start(transaction, true);
		return transaction;
	}

	@Override
	public Trace newTrace(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		return new DefaultTrace(type, name, m_manager);
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		DefaultTransaction transaction = new DefaultTransaction(type, name, m_manager);

		m_manager.start(transaction, false);
		return transaction;
	}

	public Transaction newTransaction(Transaction parent, String type, String name) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		DefaultTransaction transaction = new DefaultTransaction(type, name, m_manager);

		parent.addChild(transaction);
		transaction.setStandalone(false);
		return transaction;
	}

	private boolean shouldLog(Throwable e) {
		if (m_manager instanceof DefaultMessageManager) {
			return ((DefaultMessageManager) m_manager).shouldLog(e);
		} else {
			return true;
		}
	}
}
