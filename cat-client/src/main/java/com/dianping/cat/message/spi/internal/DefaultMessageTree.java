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
package com.dianping.cat.message.spi.internal;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.io.BufReleaseHelper;
import io.netty.buffer.ByteBuf;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class DefaultMessageTree implements MessageTree {

	private ByteBuf m_buf;

	private String m_domain;

	private String m_hostName;

	private String m_ipAddress;

	private Message m_message;

	private String m_messageId;

	private String m_parentMessageId;

	private String m_rootMessageId;

	private String m_sessionToken;

	private String m_threadGroupName;

	private String m_threadId;

	private String m_threadName;

	private MessageId m_formatMessageId;

	private boolean m_discard = true;

	private boolean m_processLoss = false;

	private boolean m_hitSample = false;

	private List<Event> events = new ArrayList<Event>();

	private List<Transaction> transactions = new ArrayList<Transaction>();

	private List<Heartbeat> heartbeats = new ArrayList<Heartbeat>();

	private List<Metric> metrics = new ArrayList<Metric>();

	@Override
	public boolean canDiscard() {
		return m_discard;
	}

	@Override
	public MessageTree copy() {
		MessageTree tree = new DefaultMessageTree();

		tree.setDomain(m_domain);
		tree.setHostName(m_hostName);
		tree.setIpAddress(m_ipAddress);
		tree.setMessageId(m_messageId);
		tree.setParentMessageId(m_parentMessageId);
		tree.setRootMessageId(m_rootMessageId);
		tree.setSessionToken(m_sessionToken);
		tree.setThreadGroupName(m_threadGroupName);
		tree.setThreadId(m_threadId);
		tree.setThreadName(m_threadName);
		tree.setMessage(m_message);
		tree.setDiscardPrivate(m_discard);
		tree.setHitSample(m_hitSample);
		return tree;
	}

	public List<Event> findOrCreateEvents() {
		if (events == null) {
			events = new ArrayList<Event>();
		}
		return events;
	}

	public List<Heartbeat> findOrCreateHeartbeats() {
		if (heartbeats == null) {
			heartbeats = new ArrayList<Heartbeat>();
		}
		return heartbeats;
	}

	public List<Metric> findOrCreateMetrics() {
		if (metrics == null) {
			metrics = new ArrayList<Metric>();
		}
		return metrics;
	}

	public List<Transaction> findOrCreateTransactions() {
		if (transactions == null) {
			transactions = new ArrayList<Transaction>();
		}
		return transactions;
	}

	public MessageTree copyForTest() {
		ByteBuf buf = null;
		try {
			PlainTextMessageCodec codec = new PlainTextMessageCodec();
			buf = codec.encode(this);
			//buf.readInt(); // get rid of length

			return codec.decode(buf);
		} catch (Exception ex) {
			Cat.logError(ex);
		}

		return null;
	}

	public void clearMessageList() {
		if (transactions != null) {
			transactions.clear();
		}

		if (events != null) {
			events.clear();
		}

		if (heartbeats != null) {
			heartbeats.clear();
		}

		if (metrics != null) {
			metrics.clear();
		}
	}

	public ByteBuf getBuffer() {
		return m_buf;
	}

	public void setBuffer(ByteBuf buf) {
		m_buf = buf;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public void setDomain(String domain) {
		m_domain = domain;
	}

	public List<Event> getEvents() {
		return events;
	}

	public MessageId getFormatMessageId() {
		if (m_formatMessageId == null) {
			m_formatMessageId = MessageId.parse(m_messageId);
		}

		return m_formatMessageId;
	}

	public void setFormatMessageId(MessageId formatMessageId) {
		m_formatMessageId = formatMessageId;
	}

	public List<Heartbeat> getHeartbeats() {
		return heartbeats;
	}

	@Override
	public String getHostName() {
		return m_hostName;
	}

	@Override
	public void setHostName(String hostName) {
		m_hostName = hostName;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	@Override
	public String getSessionToken() {
		return m_sessionToken;
	}

	@Override
	public void setSessionToken(String sessionToken) {
		m_sessionToken = sessionToken;
	}

	@Override
	public Message getMessage() {
		return m_message;
	}

	@Override
	public void setMessage(Message message) {
		m_message = message;
	}

	@Override
	public String getMessageId() {
		return m_messageId;
	}

	@Override
	public void setMessageId(String messageId) {
		if (messageId != null && messageId.length() > 0) {
			m_messageId = messageId;
		}
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	@Override
	public String getParentMessageId() {
		return m_parentMessageId;
	}

	@Override
	public void setParentMessageId(String parentMessageId) {
		if (parentMessageId != null && parentMessageId.length() > 0) {
			m_parentMessageId = parentMessageId;
		}
	}

	@Override
	public String getRootMessageId() {
		return m_rootMessageId;
	}

	@Override
	public void setRootMessageId(String rootMessageId) {
		if (rootMessageId != null && rootMessageId.length() > 0) {
			m_rootMessageId = rootMessageId;
		}
	}

	@Override
	public String getThreadGroupName() {
		return m_threadGroupName;
	}

	@Override
	public void setThreadGroupName(String threadGroupName) {
		m_threadGroupName = threadGroupName;
	}

	@Override
	public String getThreadId() {
		return m_threadId;
	}

	@Override
	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	@Override
	public String getThreadName() {
		return m_threadName;
	}

	@Override
	public void setThreadName(String threadName) {
		m_threadName = threadName;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	@Override
	public boolean isProcessLoss() {
		return m_processLoss;
	}

	@Override
	public void setProcessLoss(boolean loss) {
		m_processLoss = loss;
	}

	public void setDiscard(boolean discard) {
		m_discard = discard;
	}

	@Override
	public boolean isHitSample() {
		return m_hitSample;
	}

	@Override
	public void setHitSample(boolean hitSample) {
		m_hitSample = hitSample;
	}

	public void setDiscardPrivate(boolean discard) {
		m_discard = discard;
	}

	@Override
	public String toString() {
		ByteBuf buf = null;
		String result = "";
		try {
			PlainTextMessageCodec codec = new PlainTextMessageCodec();
			buf = codec.encode(this);
			buf.readInt(); // get rid of length
			result = buf.toString(Charset.forName("utf-8"));
		} catch (Exception ex) {
			Cat.logError(ex);
		} finally {
			if (buf != null) {
				BufReleaseHelper.release(buf);
			}
		}

        return result;
    }


}
