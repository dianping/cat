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
package com.dianping.cat.demo;

import org.junit.Test;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class ThreadTest {

	@Test
	public void test() throws InterruptedException {
		Transaction t = Cat.newTransaction("test3", "test3");

		String id = Cat.getProducer().createMessageId();

		Threads.forGroup("cat").start(new Task(id));

		Cat.logEvent("RemoteLink", "ChildThread3", Event.SUCCESS, id);

		t.complete();

		Thread.sleep(1000);
	}

	public static class Task implements Runnable {

		private String m_messageId;

		public Task(String id) {
			m_messageId = id;
		}

		@Override
		public void run() {

			Transaction t = Cat.newTransaction("test2", "test2");

			Cat.getManager().getThreadLocalMessageTree().setMessageId(m_messageId);

			t.complete();
		}
	}
}
