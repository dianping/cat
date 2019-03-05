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

import com.dianping.cat.message.*;
import com.dianping.cat.message.spi.internal.NullMessageTree;

import java.util.concurrent.atomic.AtomicInteger;

public class NullMessageProducer implements MessageProducer {
    public static final NullMessageProducer NULL_MESSAGE_PRODUCER = new NullMessageProducer();
    private AtomicInteger seq = new AtomicInteger(0);

    @Override
    public String createMessageId() {
        return NullMessageTree.UNKNOWN + "-00000000-000000-" + seq.get();
    }

    @Override
    public String createRpcServerId(String domain) {
        return createMessageId();
    }

    @Override
    public void logError(String message, Throwable cause) {

    }

    @Override
    public void logError(Throwable cause) {

    }

    @Override
    public void logErrorWithCategory(String category, String message, Throwable cause) {
    }

    @Override
    public void logErrorWithCategory(String category, Throwable cause) {
    }

    @Override
    public void logEvent(String type, String name) {
    }

    @Override
    public void logEvent(String type, String name, String status, String nameValuePairs) {
    }

    @Override
    public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
    }

    @Override
    public void logMetric(String name, String status, String nameValuePairs) {
    }

    @Override
    public Event newEvent(String type, String name) {
        return NullMessage.EVENT;
    }

    @Override
    public Heartbeat newHeartbeat(String type, String name) {
        return NullMessage.HEARTBEAT;
    }

    @Override
    public Metric newMetric(String type, String name) {
        return NullMessage.METRIC;
    }

    @Override
    public Trace newTrace(String type, String name) {
        return NullMessage.TRACE;
    }

    @Override
    public Transaction newTransaction(String type, String name) {
        return NullMessage.TRANSACTION;
    }

}
