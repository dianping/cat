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
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.MessageContext;
import com.dianping.cat.message.tree.MessageTree;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private MessageContext m_ctx;

	private volatile long m_durationInMicros;

	private List<Message> m_children;

	private boolean m_standalone;

	private DefaultTransaction(DefaultTransaction t) {
		super(t.getType(), t.getName());

		m_durationInMicros = t.m_durationInMicros;

		setStatus(t.getStatus());
		setTimestamp(t.getTimestamp());
		setData(t.getData());
	}

	public DefaultTransaction(MessageContext ctx, String type, String name) {
		super(type, name);

		m_ctx = ctx;
		m_durationInMicros = System.nanoTime() / 1000L;
		m_ctx.start(this);
	}

	public DefaultTransaction(String type, String name) {
		super(type, name);
	}

	@Override
	public DefaultTransaction addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		if (message != null) {
			m_children.add(message);
		} else {
			Cat.logError(new Exception("Null child message."));
		}

		return this;
	}

	@Override
	public void complete() {
		if (m_durationInMicros > 1e9) { // duration is not set
			long end = System.nanoTime();

			m_durationInMicros = end / 1000L - m_durationInMicros;
		}

		super.setCompleted();

		if (m_children != null) {
			List<Message> children = new ArrayList<Message>(m_children);

			for (Message child : children) {
				if (!child.isCompleted() && child instanceof ForkableTransaction) {
					child.complete();
				}
			}
		}

		m_ctx.end(this);
	}

	@Override
	public void complete(long startInMillis, long endInMillis) {
		setTimestamp(startInMillis);
		setDurationInMillis(endInMillis - startInMillis);

		super.setCompleted();

		if (m_children != null) {
			for (Message child : m_children) {
				if (!child.isCompleted() && child instanceof ForkableTransaction) {
					child.complete();
				}
			}
		}

		m_ctx.end(this);
	}

	@Override
	public ForkableTransaction forFork() {
		MessageTree tree = m_ctx.getMessageTreeWithMessageId();
		String rootMessageId = tree.getRootMessageId();
		String messageId = tree.getMessageId();
		ForkableTransaction forkable = new DefaultForkableTransaction(rootMessageId, messageId);

		addChild(forkable);
		return forkable;
	}

	@Override
	public List<Message> getChildren() {
		if (m_children == null) {
			return Collections.emptyList();
		} else {
			return m_children;
		}
	}

	@Override
	public long getDurationInMicros() {
		if (super.isCompleted()) {
			return m_durationInMicros;
		} else {
			return 0;
		}
	}

	@Override
	public long getDurationInMillis() {
		if (super.isCompleted()) {
			return m_durationInMicros / 1000L;
		} else {
			return 0;
		}
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
		m_durationInMicros = duration;
	}

	@Override
	public void setDurationInMillis(long duration) {
		m_durationInMicros = duration * 1000L;
	}

	public void setStandalone(boolean standalone) {
		m_standalone = standalone;
	}

	public DefaultTransaction shallowCopy() {
		return new DefaultTransaction(this);
	}
}
