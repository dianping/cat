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
package com.dianping.cat;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.Transaction;

public class CatInitTest {

	@Test
	public void testInitCat() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(100);
		for (int i = 0; i < 50; i++) {
			Threads.forGroup("cat").start(new InitTask(latch));
			latch.countDown();
		}
		for (int i = 0; i < 50; i++) {

			Threads.forGroup("cat").start(new InitWithJobTask(latch));
			latch.countDown();
		}

		Thread.sleep(50 * 1000);
	}

	public static class InitTask implements Task {

		private CountDownLatch m_latch;

		public InitTask(CountDownLatch latch) {
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Transaction t = Cat.newTransaction("test", "test");

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t.complete();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {

		}
	}

	public static class InitWithJobTask implements Task {

		private CountDownLatch m_latch;

		public InitWithJobTask(CountDownLatch latch) {
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Cat.initializeByDomain("cat", "127.0.0.1", "127.0.0.2");

			Transaction t = Cat.newTransaction("test", "test");

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t.complete();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {

		}
	}

}
