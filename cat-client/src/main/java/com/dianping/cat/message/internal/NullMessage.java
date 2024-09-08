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

import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

public enum NullMessage implements Transaction, Event, Trace, Heartbeat {
	TRANSACTION,

	EVENT,

	TRACE,

	HEARTBEAT;

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
	public void complete() {
	}

	@Override
	public List<Message> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public Object getData() {
		return null;
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
	public void setDurationInMillis(long durationInMills) {
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	public String getParentMessageId() {
		return null;
	}

	public String getRootMessageId() {
		return null;
	}

	@Override
	public String getStatus() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatus(Throwable e) {
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType() {
		throw new UnsupportedOperationException();
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
	public void setStatus(String status) {
	}

	@Override
	public Message success() {
		return this;
	}

	@Override
	public void complete(long startInMillis, long endInMillis) {
	}

	@Override
	public ForkableTransaction forFork() {
		return new DefaultForkableTransaction(null, null);
	}

	@Override
	public void complete(long startInMillis) {
	}
}
