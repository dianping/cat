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

import java.nio.charset.Charset;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.encoder.PlainTextMessageTreeEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class DefaultMessageTree implements MessageTree {
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

	public DefaultMessageTree copy() {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain(m_domain);
		tree.setSessionToken(m_sessionToken);
		tree.setHostName(m_hostName);
		tree.setIpAddress(m_ipAddress);
		tree.setMessageId(m_messageId);
		tree.setParentMessageId(m_parentMessageId);
		tree.setRootMessageId(m_rootMessageId);
		tree.setThreadGroupName(m_threadGroupName);
		tree.setThreadId(m_threadId);
		tree.setThreadName(m_threadName);
		tree.setMessage(m_message);

		return tree;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public String getHostName() {
		return m_hostName;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	@Override
	public Message getMessage() {
		return m_message;
	}

	@Override
	public String getMessageId() {
		return m_messageId;
	}

	@Override
	public String getParentMessageId() {
		return m_parentMessageId;
	}

	@Override
	public String getRootMessageId() {
		return m_rootMessageId;
	}

	@Override
	public String getSessionToken() {
		return m_sessionToken;
	}

	@Override
	public String getThreadGroupName() {
		return m_threadGroupName;
	}

	@Override
	public String getThreadId() {
		return m_threadId;
	}

	@Override
	public String getThreadName() {
		return m_threadName;
	}

	public void reset() {
		m_message = null;
		m_messageId = null;
		m_parentMessageId = null;
		m_rootMessageId = null;
	}

	@Override
	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public void setHostName(String hostName) {
		m_hostName = hostName;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	@Override
	public void setMessage(Message message) {
		m_message = message;
	}

	@Override
	public void setMessageId(String messageId) {
		if (messageId != null && messageId.length() > 0) {
			m_messageId = messageId;
		}
	}

	@Override
	public void setParentMessageId(String parentMessageId) {
		if (parentMessageId != null && parentMessageId.length() > 0) {
			m_parentMessageId = parentMessageId;
		}
	}

	@Override
	public void setRootMessageId(String rootMessageId) {
		if (rootMessageId != null && rootMessageId.length() > 0) {
			m_rootMessageId = rootMessageId;
		}
	}

	@Override
	public void setSessionToken(String sessionToken) {
		m_sessionToken = sessionToken;
	}

	@Override
	public void setThreadGroupName(String threadGroupName) {
		m_threadGroupName = threadGroupName;
	}

	@Override
	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	@Override
	public void setThreadName(String threadName) {
		m_threadName = threadName;
	}

	@Override
	public String toString() {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10 * 1024); // 10K

		new PlainTextMessageTreeEncoder().encode(this, buf);

		return buf.toString(Charset.forName("utf-8"));
	}

}
