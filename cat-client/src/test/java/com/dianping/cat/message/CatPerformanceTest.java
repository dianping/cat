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
package com.dianping.cat.message;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

import static com.dianping.cat.message.Message.SUCCESS;

@RunWith(JUnit4.class)
public class CatPerformanceTest {

	private static int error = 0;

	private int count = 100000;

	private int threadNumber = 4;

	@Before
	public void before() {

	}

	@After
	public void after() {

	}

	private void creatInternal() {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL2", "WebPage");
		String id1 = cat.createMessageId();
		String id2 = cat.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private void creatOneTransaction() {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL4", "WebPage");
		String id1 = cat.createMessageId();
		String id2 = cat.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			creatInternal();
			cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Test
	@Ignore
	public void justloop() throws InterruptedException {
		initClient();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						creatOneTransaction();
						LockSupport.parkNanos(10);
					}
				} finally {
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						creatOneTransaction();
						LockSupport.parkNanos(10);
					}
				} finally {
				}
			}
		}).start();

		Thread.sleep(1000000);

	}

	@Test
	@Ignore
	public void justloop2() throws InterruptedException {
		initClient();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					creatOneTransaction();
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					creatOneTransaction();
				}
			}
		}).start();

		Thread.sleep(1000000);

	}

	@Ignore
	@Test
	public void test() throws InterruptedException {
		initClient();
		long time = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			creatOneTransaction();
		}
		long endtime = System.currentTimeMillis();

		System.out.println("avg:" + (double) (endtime - time) / (double) count + "ms");
		Thread.sleep(1000000);
	}

	@Test
	@Ignore
	public void testManyThread() throws IOException, InterruptedException {
		initClient();
		System.out.println("press any key to continue...");
		System.in.read();

		CountDownLatch start = new CountDownLatch(threadNumber);
		CountDownLatch end = new CountDownLatch(threadNumber);
		for (int i = 0; i < threadNumber; i++) {
			TestThread thread = new TestThread(start, end);
			thread.start();
			start.countDown();
		}
		try {
			end.await();
		} catch (InterruptedException e) {
			// ignore
		}

		System.out.println("Done with errors: " + error);
		Thread.sleep(10000);
	}

	private void initClient() {
		Cat.initialize(new File(Cat.getCatHome(),"client.xml"));
	}

	class TestThread extends Thread {

		CountDownLatch m_end;

		CountDownLatch m_latch;

		public TestThread(CountDownLatch latch, CountDownLatch end) {
			m_latch = latch;
			m_end = end;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				// ignore
			}
			long time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				creatOneTransaction();
			}
			long endtime = System.currentTimeMillis();

			System.out.println(Thread.currentThread().getName() + " avg: " + (double) (endtime - time) / (double) count	+ "ms");
			m_end.countDown();
		}
	}

}
