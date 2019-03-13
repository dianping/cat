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

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.NullMessage;
import com.dianping.cat.message.spi.MessageTree;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class NullMessageTree implements MessageTree {
    public static final NullMessageTree NULL_MESSAGE_TREE = new NullMessageTree();

    public static final String UNKNOWN = "Unknown";

    public static final String UNKNOWN_MESSAGE_ID = UNKNOWN + "-00000000-000000-0";

    @Override
    public void addForkableTransaction(ForkableTransaction forkableTransaction) {

    }

    @Override
    public boolean canDiscard() {
        return false;
    }

    @Override
    public MessageTree copy() {
        return this;
    }

    @Override
    public List<Event> findOrCreateEvents() {
        return new ArrayList<Event>();
    }

    @Override
    public List<Heartbeat> findOrCreateHeartbeats() {
        return new ArrayList<Heartbeat>();
    }

    @Override
    public List<Metric> findOrCreateMetrics() {
        return new ArrayList<Metric>();
    }

    @Override
    public List<Transaction> findOrCreateTransactions() {
        return new ArrayList<Transaction>();
    }

    @Override
    public ByteBuf getBuffer() {
        return null;
    }

    @Override
    public String getDomain() {
        return UNKNOWN;
    }

    @Override
    public List<Event> getEvents() {
        return new ArrayList<Event>();
    }

    @Override
    public List<ForkableTransaction> getForkableTransactions() {
        return null;
    }

    @Override
    public MessageId getFormatMessageId() {
        return null;
    }

    @Override
    public List<Heartbeat> getHeartbeats() {
        return new ArrayList<Heartbeat>();
    }

    @Override
    public String getHostName() {
        return UNKNOWN;
    }

    @Override
    public String getIpAddress() {
        return NetworkInterfaceManager.LOOPBACK;
    }

    @Override
    public Message getMessage() {
        return NullMessage.TRANSACTION;
    }

    @Override
    public String getMessageId() {
        // cat-0a08722f-399628-8613
        return UNKNOWN_MESSAGE_ID;
    }

    @Override
    public List<Metric> getMetrics() {
        return new ArrayList<Metric>();
    }

    @Override
    public String getParentMessageId() {
        return UNKNOWN_MESSAGE_ID;
    }

    @Override
    public String getRootMessageId() {
        return UNKNOWN_MESSAGE_ID;
    }

    @Override
    public String getSessionToken() {
        return UNKNOWN;
    }

    @Override
    public String getThreadGroupName() {
        return UNKNOWN;
    }

    @Override
    public String getThreadId() {
        return "0";
    }

    @Override
    public String getThreadName() {
        return UNKNOWN;
    }

    @Override
    public List<Transaction> getTransactions() {
        return new ArrayList<Transaction>();
    }

    @Override
    public boolean isHitSample() {
        return false;
    }

    @Override
    public void setDiscardPrivate(boolean sample) {

    }

    @Override
    public void setDomain(String domain) {

    }

    @Override
    public void setFormatMessageId(MessageId messageId) {
    }

    @Override
    public void setHitSample(boolean hitSample) {

    }

    @Override
    public void setHostName(String hostName) {

    }

    @Override
    public void setIpAddress(String ipAddress) {

    }

    @Override
    public void setMessage(Message message) {

    }

    @Override
    public void setMessageId(String messageId) {

    }

    @Override
    public void setParentMessageId(String parentMessageId) {

    }

    @Override
    public void setRootMessageId(String rootMessageId) {

    }

    @Override
    public void setSessionToken(String sessionToken) {

    }

    @Override
    public void setThreadGroupName(String name) {

    }

    @Override
    public void setThreadId(String threadId) {

    }

    @Override
    public void setThreadName(String id) {

    }

}
