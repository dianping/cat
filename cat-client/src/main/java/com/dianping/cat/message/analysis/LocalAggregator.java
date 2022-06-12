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
package com.dianping.cat.message.analysis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.util.Threads.Task;

// Component
public class LocalAggregator implements Task, Initializable {
	// Inject
	private TransactionAggregator m_transactionAggregator;

	// Inject
	private EventAggregator m_eventAggregator;

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	private CountDownLatch m_latch = new CountDownLatch(1);

	@Override
	public String getName() {
		return getClass().getName();
	}

	public void handle(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			handleTransaction((Transaction) message);
		} else if (message instanceof Event) {
			m_eventAggregator.logEvent((Event) message);
		}
	}

	private void handleTransaction(Transaction transaction) {
		m_transactionAggregator.logTransaction(transaction);

		for (Message child : transaction.getChildren()) {
			if (child instanceof Transaction) {
				handleTransaction((Transaction) child);
			} else if (child instanceof Event) {
				m_eventAggregator.logEvent((Event) child);
			}
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_transactionAggregator = ctx.lookup(TransactionAggregator.class);
		m_eventAggregator = ctx.lookup(EventAggregator.class);
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				long start = System.currentTimeMillis();

				try {
					m_transactionAggregator.sendTransactionData();
					m_eventAggregator.sendEventData();
				} catch (Exception ex) {
					Cat.logError(ex);
				}

				long duration = System.currentTimeMillis() - start;

				if (duration >= 0 && duration < 1000) {
					TimeUnit.MILLISECONDS.sleep(1000 - duration);
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		} finally {
			m_latch.countDown();
		}
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	public boolean isAtomicMessage(MessageTree tree) {
		return m_transactionAggregator.isAtomicMessage(tree);
	}
}
