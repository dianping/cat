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
import java.util.Collections;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private long m_durationInMicro = -1; // must be less than 0

	private List<Message> m_children;

	private MessageManager m_manager;

	private boolean m_standalone;

	private long m_durationStart;

	public DefaultTransaction(String type, String name) {
		super(type, name);
		m_durationStart = System.nanoTime();
	}

	public DefaultTransaction(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
		m_standalone = true;
		m_durationStart = System.nanoTime();
	}

	@Override
	public DefaultTransaction addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		if (message != null) {
			m_children.add(message);
		} else {
			Cat.logError(new Exception("null child message"));
		}
		return this;
	}

	@Override
	public void complete() {
		try {
			if (isCompleted()) {
				// complete() was called more than once
				DefaultEvent event = new DefaultEvent("cat", "BadInstrument");

				event.setStatus("TransactionAlreadyCompleted");
				event.complete();
				addChild(event);
			} else {
				if (m_durationInMicro == -1) {
					m_durationInMicro = (System.nanoTime() - m_durationStart) / 1000L;
				}
				setCompleted(true);
				if (m_manager != null) {
					m_manager.end(this);
				}
			}
		} catch (Exception e) {
			// ignore
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

					duration = (trx.getTimestamp() - getTimestamp()) * 1000L;
				} else {
					duration = (lastChild.getTimestamp() - getTimestamp()) * 1000L;
				}
			}

			return duration;
		}
	}

	public void setDurationInMicros(long duration) {
		m_durationInMicro = duration;
	}

	@Override
	public long getDurationInMillis() {
		return getDurationInMicros() / 1000L;
	}

	@Override
	public void setDurationInMillis(long duration) {
		m_durationInMicro = duration * 1000L;
	}

	protected MessageManager getManager() {
		return m_manager;
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	@Override
	public boolean isStandalone() {
		return m_standalone;
	}

	public void setStandalone(boolean standalone) {
		m_standalone = standalone;
	}

	public void setDurationStart(long durationStart) {
		m_durationStart = durationStart;
	}

	@Override
	public void setStatus(Throwable e) {
		m_status = e.getClass().getName();
		m_manager.getThreadLocalMessageTree().setDiscard(false);
	}
}
