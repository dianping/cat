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
import java.util.concurrent.atomic.AtomicLong;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.Transaction;

public class CatPerformanceTest {

	public static volatile AtomicLong m_totalTime = new AtomicLong(0);

	public static volatile AtomicLong m_bussniessTime = new AtomicLong(0);

	// 每个线程执行的cat api次数
	public static int m_perTheadExecuteTime = 10000;

	public static void autoTest() throws InterruptedException {
		testGroup(1);
		testGroup(2);
		testGroup(5);
		testGroup(50);
	}

	public static void testGroup(int duration) throws InterruptedException {
		test(10, duration);
		test(20, duration);
		test(50, duration);
		test(100, duration);
		test(200, duration);
		test(500, duration);
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		setUp();

		if (args.length == 0) {
			autoTest();
		} else {
			int totalThreadCount = Integer.parseInt(args[0]);
			int sleepTime = Integer.parseInt(args[1]);

			test(totalThreadCount, sleepTime);
		}
	}

	private static void test(int totalThreadCount, int sleepTime) throws InterruptedException {
		System.out.println("开启压测线程个数:" + totalThreadCount + " 业务代码消耗时间:" + sleepTime + "(ms)");
		CountDownLatch latch = new CountDownLatch(totalThreadCount);

		m_totalTime = new AtomicLong(0);
		m_bussniessTime = new AtomicLong(0);

		for (int index = 0; index < totalThreadCount; index++) {
			Threads.forGroup("cat").start(new TestThread(index, sleepTime, latch));
		}

		latch.await();

		System.out.println("=====打印最后结果=====");
		printResult();
		System.out.println("====================");
	}

	private static void doBussniess(int avg) throws InterruptedException {
		Thread.sleep(avg);
	}

	public static void processWithCatApi(int index, int avg) throws InterruptedException {
		for (int i = 1; i < m_perTheadExecuteTime; i++) {
			long start = System.currentTimeMillis(); // 记录开始时间
			Transaction t0 = Cat.newTransaction("test", "test0");
			Transaction t1 = Cat.newTransaction("test", "test1");
			Transaction t2 = Cat.newTransaction("test", "test2");
			Transaction t3 = Cat.newTransaction("test", "test3");
			Transaction t4 = Cat.newTransaction("test", "test4");
			Transaction t5 = Cat.newTransaction("test", "test5");
			Transaction t6 = Cat.newTransaction("test", "test6");
			Transaction t7 = Cat.newTransaction("test", "test7");
			Transaction t8 = Cat.newTransaction("test", "test8");
			Transaction t9 = Cat.newTransaction("test", "test9");

			long bussinessStart = System.currentTimeMillis(); // 记录业务开始时间
			doBussniess(avg);
			long duration = System.currentTimeMillis() - bussinessStart; // 记录业务结束时间

			t0.setStatus(Transaction.SUCCESS);
			t1.setStatus(Transaction.SUCCESS);
			t2.setStatus(Transaction.SUCCESS);
			t3.setStatus(Transaction.SUCCESS);
			t4.setStatus(Transaction.SUCCESS);
			t5.setStatus(Transaction.SUCCESS);
			t6.setStatus(Transaction.SUCCESS);
			t7.setStatus(Transaction.SUCCESS);
			t8.setStatus(Transaction.SUCCESS);
			t9.setStatus(Transaction.SUCCESS);
			t9.complete();
			t8.complete();
			t7.complete();
			t6.complete();
			t5.complete();
			t4.complete();
			t3.complete();
			t2.complete();
			t1.complete();
			t0.complete();

			m_totalTime.addAndGet((System.currentTimeMillis() - start));
			m_bussniessTime.addAndGet(duration);

			if (i % 1000 == 0) { // 每次100次打印一次cat的平均消耗
				// printResult();
			}
		}
	}

	private static void printResult() {
		double catCost = 1 - m_bussniessTime.get() * 1.0 / m_totalTime.get();

		System.out.println(
								"总时间消耗:" + m_totalTime.get() + "(ms) 业务代码消耗:" + m_bussniessTime + "(ms) CAT消耗比例:"	+ new DecimalFormat("0.00%")
														.format(catCost));
	}

	public static void setUp() {
		Transaction t = Cat.newTransaction("PerformanceTest", "PerformanceTest");

		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	public static class TestThread implements Task {

		private int m_index;

		private int m_sleepTime;

		private CountDownLatch m_latch;

		public TestThread(int index, int sleepTime, CountDownLatch latch) {
			m_index = index;
			m_sleepTime = sleepTime;
			m_latch = latch;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void run() {
			try {
				// 业务代码的sleep时间
				processWithCatApi(m_index, m_sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m_latch.countDown();
		}

		@Override
		public void shutdown() {

		}
	}
}
