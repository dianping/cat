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

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class TestChildrenMany {

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
	}

	@Test
	public void test() throws Exception {
		Transaction t = Cat.newTransaction("Check1", "name");
		Transaction t3 = Cat.newTransaction("Check2", "name");
		for (int i = 0; i < 2080; i++) {
			Transaction t4 = Cat.newTransaction("Check3", "name");
			t4.complete();
		}
		t3.complete();
		t.complete();
		Thread.sleep(1000);
	}

	@Test
	public void testEvent() throws Exception {
		while (true) {
			for (int i = 0; i < 1000; i++) {
				Cat.logEvent("Event", "Event", Event.SUCCESS, null);

				Transaction t = Cat.newTransaction("Cache.mem", "mem");

				t.complete();
			}
			Thread.sleep(1000);
		}
	}

	@Test
	public void testReuseId() throws Exception {
		for (int i = 0; i < 201; i++) {
			Cat.logEvent("Event", "Event", Event.SUCCESS, null);
		}

		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("te", "tei");

			t.complete();
		}

		Thread.sleep(100000);
	}

	@Test
	public void testManyThread() throws Exception {
		System.setProperty("devMode", "true");
		int size = 10;
		CountDownLatch latch = new CountDownLatch(size);
		for (int i = 0; i < size; i++) {
			Threads.forGroup("cat").start(new SendEvent(i, latch));
		}

		Thread.sleep(100000);
	}

	public static class SendEvent implements Task {

		private int m_index;

		private CountDownLatch m_latch;

		public SendEvent(int index, CountDownLatch latch) {
			m_index = index;
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.countDown();
				m_latch.await();

				for (int i = 0; i < 10000; i++) {
					Cat.logEvent("BEvent" + m_index, "Event" + m_index, Event.SUCCESS, null);
					Cat.logMetricForCount("test");
				}

				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getName() {
			return "index:" + m_index;
		}

		@Override
		public void shutdown() {
		}
	}

}
