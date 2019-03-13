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

import java.util.ArrayList;
import java.util.List;

public enum NullMessage implements Transaction, Event, Metric, Trace, Heartbeat, ForkedTransaction, ForkableTransaction {
    TRANSACTION,
    EVENT,
    METRIC,
    TRACE,
    HEARTBEAT,
    FORKED_TRANSACTION;

    private static String DEFAULT = "";

    @Override
    public Transaction addChild(Message message) {
        return this;
    }

    @Override
    public void addData(String keyValuePairs) {
    }

    @Override
    public void addData(String key, Object value) {
    }

    @Override
    public void close() {

    }

    @Override
    public void complete() {
    }

    @Override
    public ForkedTransaction doFork() {
        return this;
    }

    @Override
    public ForkableTransaction forFork() {
        return this;
    }

    @Override
    public List<Message> getChildren() {
        return new ArrayList<Message>();
    }

    @Override
    public Object getData() {
        return DEFAULT;
    }

    @Override
    public long getDurationInMicros() {
        return 0;
    }

    @Override
    public long getDurationInMillis() {
        return 0;
    }

    @Override
    public String getMessageId() {
        return DEFAULT;
    }

    @Override
    public String getName() {
        return DEFAULT;
    }

    public String getParentMessageId() {
        return DEFAULT;
    }

    public long getRawDurationInMicros() {
        return 0;
    }

    public String getRootMessageId() {
        return DEFAULT;
    }

    @Override
    public String getStatus() {
        return DEFAULT;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public String getType() {
        return DEFAULT;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public Message join() {
        return this;
    }

    @Override
    public void setDurationInMicros(long durationInMicros) {
    }

    @Override
    public void setDurationInMillis(long duration) {
    }

    @Override
    public void setDurationStart(long durationStart) {
    }

    @Override
    public void setMessageId(String messageId) {
    }

    @Override
    public void setStatus(String status) {
    }

    @Override
    public void setStatus(Throwable e) {
    }

    @Override
    public void setSuccessStatus() {
    }

    @Override
    public void setTimestamp(long timestamp) {
    }

}
