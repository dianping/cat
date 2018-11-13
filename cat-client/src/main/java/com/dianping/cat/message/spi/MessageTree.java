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
package com.dianping.cat.message.spi;

import java.util.List;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;

public interface MessageTree extends Cloneable {
	public boolean canDiscard();

	public MessageTree copy();

	public List<Event> findOrCreateEvents();

	public List<Heartbeat> findOrCreateHeartbeats();

	public List<Metric> findOrCreateMetrics();

	public List<Transaction> findOrCreateTransactions();

	public ByteBuf getBuffer();

	public String getDomain();

	public void setDomain(String domain);

	public List<Event> getEvents();

	public MessageId getFormatMessageId();

	public void setFormatMessageId(MessageId messageId);

	public List<Heartbeat> getHeartbeats();

	public String getHostName();

	public void setHostName(String hostName);

	public String getIpAddress();

	public void setIpAddress(String ipAddress);

	public String getSessionToken();

	public void setSessionToken(String session);

	public Message getMessage();

	public void setMessage(Message message);

	public String getMessageId();

	public void setMessageId(String messageId);

	public List<Metric> getMetrics();

	public String getParentMessageId();

	public void setParentMessageId(String parentMessageId);

	public String getRootMessageId();

	public void setRootMessageId(String rootMessageId);

	public String getThreadGroupName();

	public void setThreadGroupName(String name);

	public String getThreadId();

	public void setThreadId(String threadId);

	public String getThreadName();

	public void setThreadName(String id);

	public List<Transaction> getTransactions();

	public boolean isProcessLoss();

	public void setProcessLoss(boolean loss);

	public void setDiscard(boolean discard);

	public boolean isHitSample();

	public void setHitSample(boolean hitSample);

	public void setDiscardPrivate(boolean discard);

}
