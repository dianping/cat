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
package com.dianping.cat.storage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.ReportBucket;

public abstract class StringBucketTestCase extends ComponentTestCase {

	protected final static int threadNum = 200;// notice: max 9, for creating asc order id bellow

	protected final static int timesPerThread = 1000; // notice: must be powers 10, fro creating asc order id bellow

	protected ExecutorService pool = null;

	protected ReportBucket bucket = null;

	protected void printFails(final int fails, final long start) {
		System.out.println(
								new Throwable().getStackTrace()[1].toString() + " threads:" + threadNum + " total:"	+ threadNum * timesPerThread
														+ " fails:" + fails + " waste:" + (System.currentTimeMillis() - start) + "ms");
		if (fails > 0) {
			Assert.fail("fails:" + fails);
		}
	}

	protected void print(final long start) {
		System.out.println(
								new Throwable().getStackTrace()[1].toString() + " threads:" + threadNum + " total:"	+ threadNum * timesPerThread
														+ " waste:" + (System.currentTimeMillis() - start) + "ms");
	}

	protected void resetSerial(final AtomicInteger serial) {
		serial.set(10 * timesPerThread);
	}

	protected AtomicInteger createSerial() {
		return new AtomicInteger(10 * timesPerThread);
	}

	protected void submit(Runnable run) {
		for (int p = 0; p < threadNum; p++) {
			pool.submit(run);
		}
	}

	protected CountDownLatch createLatch() {
		return new CountDownLatch(threadNum);
	}

	@Before
	@Override
	public void setUp() throws IOException {
		try {
			super.setUp();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			pool = Executors.newFixedThreadPool(threadNum);
			bucket = createBucket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract ReportBucket createBucket() throws Exception;

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		bucket.close();
	}

	@Test
	public void testConcurrentRead() throws Exception {
		final AtomicInteger serial = createSerial();
		final AtomicInteger fail = new AtomicInteger();
		final CountDownLatch latch = createLatch();
		this.serialWrite(serial);
		resetSerial(serial);
		long start = System.currentTimeMillis();
		submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < timesPerThread; i++) {
					String id = null;
					try {
						id = "" + serial.incrementAndGet();
						String value = "value:" + id;
						String target = bucket.findById(id);
						Assert.assertEquals(value, target);
					} catch (Throwable e) {
						e.printStackTrace();
						fail.incrementAndGet();
					}
				}
				latch.countDown();
			}
		});
		latch.await();
		printFails(fail.get(), start);
	}

	@Test
	public void testConcurrentReadWrite() throws Exception {
		final AtomicInteger serial = createSerial();
		final AtomicInteger fail = new AtomicInteger();
		final CountDownLatch latch = createLatch();
		long start = System.currentTimeMillis();
		submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < timesPerThread; i++) {
					String id = null;
					try {
						id = "" + serial.incrementAndGet();
						String value = "value:" + id;
						Assert.assertTrue(bucket.storeById(id, value));
						String target = bucket.findById(id);
						Assert.assertEquals(value, target);
					} catch (Throwable e) {
						e.printStackTrace();
						fail.incrementAndGet();
					}
				}
				latch.countDown();
			}
		});
		latch.await();
		printFails(fail.get(), start);
	}

	@Test
	public void testConcurrentWrite() throws Exception {
		final AtomicInteger serial = createSerial();
		final AtomicInteger fail = new AtomicInteger();
		final CountDownLatch latch = createLatch();
		long start = System.currentTimeMillis();
		submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < timesPerThread; i++) {
					try {
						String id = "" + serial.incrementAndGet();
						String value = "value:" + id;
						boolean success = bucket.storeById(id, value);
						if (!success) {
							fail.incrementAndGet();
						}
					} catch (Throwable e) {
						fail.incrementAndGet();
					}
				}
				latch.countDown();
			}
		});
		latch.await();
		printFails(fail.get(), start);

		resetSerial(serial);
		this.serialRead(serial);
	}

	@Test
	public void testSerialRead() throws Exception {
		final AtomicInteger serial = createSerial();
		this.serialWrite(serial);
		resetSerial(serial);
		long start = System.currentTimeMillis();
		serialRead(serial);
		print(start);
	}

	@Test
	public void testReload() throws Exception {
		final AtomicInteger serial = createSerial();
		this.serialWrite(serial);

		resetSerial(serial);

		this.bucket.close();

		long start = System.currentTimeMillis();
		bucket = createBucket();
		print(start);

		serialRead(serial);
	}

	@Test
	public void testSerialWrite() throws Exception {
		final AtomicInteger serial = createSerial();
		long start = System.currentTimeMillis();
		this.serialWrite(serial);
		print(start);
		resetSerial(serial);
		this.serialRead(serial);
	}

	private void serialRead(final AtomicInteger serial) throws IOException {
		for (int p = 0; p < threadNum; p++) {
			for (int i = 0; i < timesPerThread; i++) {
				String id = "" + serial.incrementAndGet();
				String target = bucket.findById(id);
				Assert.assertEquals("value:" + id, target);
			}
		}
	}

	private void serialWrite(AtomicInteger serial) throws IOException {
		for (int p = 0; p < threadNum; p++) {
			for (int i = 0; i < timesPerThread; i++) {
				String id = "" + serial.incrementAndGet();
				String value = "value:" + id;
				Assert.assertTrue(bucket.storeById(id, value));
			}
		}
	}

}
