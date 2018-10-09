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

import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.MessageId;
import io.netty.buffer.ByteBuf;

import java.util.List;

public interface MessageTree extends Cloneable {
    void addForkableTransaction(ForkableTransaction forkableTransaction);

    boolean canDiscard();

    MessageTree copy();

    List<Event> findOrCreateEvents();

    List<Heartbeat> findOrCreateHeartbeats();

    List<Metric> findOrCreateMetrics();

    List<Transaction> findOrCreateTransactions();

    ByteBuf getBuffer();

    String getDomain();

    List<Event> getEvents();

    List<ForkableTransaction> getForkableTransactions();

    MessageId getFormatMessageId();

    List<Heartbeat> getHeartbeats();

    String getHostName();

    String getIpAddress();

    Message getMessage();

    String getMessageId();

    List<Metric> getMetrics();

    String getParentMessageId();

    String getRootMessageId();

    String getSessionToken();

    String getThreadGroupName();

    String getThreadId();

    String getThreadName();

    List<Transaction> getTransactions();

    boolean isHitSample();

    void setDiscardPrivate(boolean discard);

    void setDomain(String domain);

    void setFormatMessageId(MessageId messageId);

    void setHitSample(boolean hitSample);

    void setHostName(String hostName);

    void setIpAddress(String ipAddress);

    void setMessage(Message message);

    void setMessageId(String messageId);

    void setParentMessageId(String parentMessageId);

    void setRootMessageId(String rootMessageId);

    void setSessionToken(String session);

    void setThreadGroupName(String name);

    void setThreadId(String threadId);

    void setThreadName(String id);

}
