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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

public class PerformanceTest {
	private AtomicLong m_allTime = new AtomicLong();

	private AtomicLong m_bizTime = new AtomicLong();

	private AtomicLong m_totalMessages = new AtomicLong();

	public static void main(String args[]) throws IOException, InterruptedException {
		PerformanceTest test = new PerformanceTest();

		if (args.length == 0) {
			test.testPerformance();
		} else {
			int totalThreadCount = Integer.parseInt(args[0]);
			int sleepTime = Integer.parseInt(args[1]);

			test.testThreads(totalThreadCount, sleepTime);
		}
	}

	@Test
	public void testPerformance() throws InterruptedException {
		long start = System.currentTimeMillis();
		int[] durations = { 1, 2, 3, 5 };
		int[] threadCounts = { 10, 50, 100, 200, 500, 1000 };

		for (int duration : durations) {
			for (int threadCount : threadCounts) {
				testThreads(threadCount, duration);
			}
		}

		long cost = System.currentTimeMillis() - start;
		System.out.println(String.format("Total messages(%s) sent in %s ms", m_totalMessages.get(), cost));
	}

	private void testThreads(int threadCount, int duration) throws InterruptedException {
		m_allTime.set(0);
		m_bizTime.set(0);

		System.out.println("Start threads(" + threadCount + ") with duration(" + duration + " ms) ...");

		ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			pool.submit(new TestThread(threadCount, duration, latch));
		}

		latch.await();
		pool.shutdown();

		double percentage = 1 - m_bizTime.get() * 1.0 / m_allTime.get();

		System.out.println("All time used: " + m_allTime.get() + " ms, biz time used:" + m_bizTime
		      + " ms, CAT time used: " + (m_allTime.get() - m_bizTime.get()) + " ms, percentage: "
		      + new DecimalFormat("0.00%").format(percentage));
		System.out.println();
	}

	@Before
	public void warmup() {
		Cat.getBootstrap().testMode();
		Cat.newTransaction("PerformanceTest", "Start").success().complete();
	}

	private class TestThread implements Task {
		private int m_id;

		private int m_duration;

		private AtomicBoolean m_enabled = new AtomicBoolean(true);

		private CountDownLatch m_latch;

		public TestThread(int id, int duration, CountDownLatch latch) {
			m_id = id;
			m_duration = duration;
			m_latch = latch;
		}

		private void doBiz() throws InterruptedException {
			Threads.sleep(m_enabled, m_duration);
		}

		private void doBizWithCat() throws InterruptedException {
			long allStart = System.nanoTime();
			String name = "Test-" + m_id;

			Transaction t0 = Cat.newTransaction(name, "Test0");
			Transaction t1 = Cat.newTransaction(name, "Test1");
			Transaction t2 = Cat.newTransaction(name, "Test2");
			Transaction t3 = Cat.newTransaction(name, "Test3");
			Transaction t4 = Cat.newTransaction(name, "Test4");
			Transaction t5 = Cat.newTransaction(name, "Test5");
			Transaction t6 = Cat.newTransaction(name, "Test6");
			Transaction t7 = Cat.newTransaction(name, "Test7");
			Transaction t8 = Cat.newTransaction(name, "Test8");
			Transaction t9 = Cat.newTransaction(name, "Test9");

			long bizStart = System.nanoTime();

			doBiz();

			long bizTime = System.nanoTime() - bizStart;

			t9.success().complete();
			t8.success().complete();
			t7.success().complete();
			t6.success().complete();
			t5.success().complete();
			t4.success().complete();
			t3.success().complete();
			t2.success().complete();
			t1.success().complete();
			t0.success().complete();

			long allTime = System.nanoTime() - allStart;

			m_allTime.addAndGet(allTime / 1000000L);
			m_bizTime.addAndGet(bizTime / 1000000L);
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < 1000; i++) {
					if (m_enabled.get()) {
						doBizWithCat();
						m_totalMessages.addAndGet(1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				m_latch.countDown();
			}
		}

		@Override
		public void shutdown() {
			m_enabled.set(false);
		}
	}
}
