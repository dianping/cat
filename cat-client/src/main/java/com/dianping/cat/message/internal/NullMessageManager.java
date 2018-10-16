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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.NullMessageTree;

public class NullMessageManager implements MessageManager, Initializable, LogEnabled {

	public static final NullMessageManager NULL_MESSAGE_MANAGER = new NullMessageManager();

	@Override
	public void initialize() throws InitializationException {
	}

	@Override
	public void enableLogging(Logger logger) {
	}

	@Override
	public void add(Message message) {
	}

	@Override
	public void end(Transaction transaction) {

	}

	@Override
	public Transaction getPeekTransaction() {
		return NullMessage.TRANSACTION;
	}

	@Override
	public MessageTree getThreadLocalMessageTree() {
		return NullMessageTree.NULL_MESSAGE_TREE;
	}

	@Override
	public boolean hasContext() {
		return false;
	}

	@Override
	public boolean isMessageEnabled() {
		return false;
	}

	@Override
	public boolean isCatEnabled() {
		return false;
	}

	@Override
	public boolean isTraceMode() {
		return false;
	}

	@Override
	public void setTraceMode(boolean traceMode) {

	}

	@Override
	public void reset() {

	}

	@Override
	public void setup() {

	}

	@Override
	public void start(Transaction transaction, boolean forked) {

	}

	@Override
	public void bind(String tag, String title) {

	}

	@Override
	public String getDomain() {
		return NullMessageTree.NULL_MESSAGE_TREE.getDomain();
	}

	@Override
	public ClientConfigManager getConfigManager() {
		return null;
	}

}
