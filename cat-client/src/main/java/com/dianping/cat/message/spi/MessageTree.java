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
    boolean canDiscard();

    MessageTree copy();

    List<Event> findOrCreateEvents();

    List<Heartbeat> findOrCreateHeartbeats();

    List<Metric> findOrCreateMetrics();

    List<Transaction> findOrCreateTransactions();

    ByteBuf getBuffer();

    String getDomain();

    void setDomain(String domain);

    List<Event> getEvents();

    MessageId getFormatMessageId();

    void setFormatMessageId(MessageId messageId);

    List<Heartbeat> getHeartbeats();

    String getHostName();

    void setHostName(String hostName);

    String getIpAddress();

    void setIpAddress(String ipAddress);

    String getSessionToken();

    void setSessionToken(String session);

    Message getMessage();

    void setMessage(Message message);

    String getMessageId();

    void setMessageId(String messageId);

    List<Metric> getMetrics();

    String getParentMessageId();

    void setParentMessageId(String parentMessageId);

    String getRootMessageId();

    void setRootMessageId(String rootMessageId);

    String getThreadGroupName();

    void setThreadGroupName(String name);

    String getThreadId();

    void setThreadId(String threadId);

    String getThreadName();

    void setThreadName(String id);

    List<Transaction> getTransactions();

    boolean isProcessLoss();

    void setProcessLoss(boolean loss);

    void setDiscard(boolean discard);

    boolean isHitSample();

    void setHitSample(boolean hitSample);

    void setDiscardPrivate(boolean discard);

}
