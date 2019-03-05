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

import com.dianping.cat.Cat;
import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class DefaultMessageTree implements MessageTree {
    private ByteBuf buf;
    private String domain;
    private String hostName;
    private String ipAddress;
    private Message message;
    private String messageId;
    private String parentMessageId;
    private String rootMessageId;
    private String sessionToken;
    private String threadGroupName;
    private String threadId;
    private String threadName;
    private MessageId formatMessageId;
    private boolean discard = true;
    private boolean hitSample = false;

    private List<Transaction> transactions;
    private List<Event> events;
    private List<Heartbeat> heartbeats;
    private List<Metric> metrics;
    private List<ForkableTransaction> forkableTransactions;

    @Override
    public synchronized void addForkableTransaction(ForkableTransaction forkableTransaction) {
        if (forkableTransactions == null) {
            forkableTransactions = new ArrayList<ForkableTransaction>();
        }

        forkableTransactions.add(forkableTransaction);
    }

    public void addHeartbeat(Heartbeat heartbeat) {
        if (heartbeats == null) {
            heartbeats = new ArrayList<Heartbeat>();
        }

        heartbeats.add(heartbeat);
    }

    @Override
    public boolean canDiscard() {
        return discard;
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public MessageTree copy() {
        MessageTree tree = new DefaultMessageTree();

        tree.setDomain(domain);
        tree.setHostName(hostName);
        tree.setIpAddress(ipAddress);
        tree.setMessageId(messageId);
        tree.setParentMessageId(parentMessageId);
        tree.setRootMessageId(rootMessageId);
        tree.setSessionToken(sessionToken);
        tree.setThreadGroupName(threadGroupName);
        tree.setThreadId(threadId);
        tree.setThreadName(threadName);
        tree.setMessage(message);
        tree.setDiscardPrivate(discard);
        tree.setHitSample(hitSample);
        return tree;
    }

    public MessageTree copyForTest() {
        ByteBuf buf = null;
        try {
            PlainTextMessageCodec codec = new PlainTextMessageCodec();
            buf = codec.encode(this);

            return codec.decode(buf);
        } catch (Exception ex) {
            Cat.logError(ex);
        }

        return null;
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

    public ByteBuf getBuffer() {
        return buf;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public List<ForkableTransaction> getForkableTransactions() {
        return forkableTransactions;
    }

    public MessageId getFormatMessageId() {
        if (formatMessageId == null) {
            formatMessageId = MessageId.parse(messageId);
        }

        return formatMessageId;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public String getParentMessageId() {
        return parentMessageId;
    }

    @Override
    public String getRootMessageId() {
        return rootMessageId;
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public String getThreadGroupName() {
        return threadGroupName;
    }

    @Override
    public String getThreadId() {
        return threadId;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public boolean isHitSample() {
        return hitSample;
    }

    public void setBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    public void setDiscardPrivate(boolean discard) {
        this.discard = discard;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setFormatMessageId(MessageId formatMessageId) {
        this.formatMessageId = formatMessageId;
    }

    @Override
    public void setHitSample(boolean hitSample) {
        this.hitSample = hitSample;
    }

    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    @Override
    public void setRootMessageId(String rootMessageId) {
        this.rootMessageId = rootMessageId;
    }

    @Override
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public void setThreadGroupName(String threadGroupName) {
        this.threadGroupName = threadGroupName;
    }

    @Override
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    @Override
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String toString() {
        return PlainTextMessageCodec.encodeTree(this);
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Heartbeat> getHeartbeats() {
        return heartbeats;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

}
