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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;

public class MessageIdFactoryTest {
	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private long m_timestamp = 1330327814748L;

	private MessageIdFactory m_factory = new MessageIdFactory() {
		@Override
		protected long getTimestamp() {
			return m_timestamp / MessageIdFactory.HOUR;
		}
	};

	@Before
	public void before() {
		if( m_factory != null ) {
			m_factory.close();
		}
		m_factory = new MessageIdFactory() {
			@Override
			protected long getTimestamp() {
				return m_timestamp / MessageIdFactory.HOUR;
			}
		};
		cleanup();
	}



	private void cleanup() {
		final List<String> paths = new ArrayList<String>();
		String base =  Cat.getCatHome();
		Scanners.forDir().scan(new File(base), new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (path.indexOf("mark") > -1) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});

		for (String path : paths) {
			File file = new File(base, path);
			boolean result = forceDelete(file);
			System.err.println("delete " + path + " " + result);
		}
	}
	
	
	public static boolean forceDelete(File f){
		boolean result = false;
		int tryCount = 0;
		while(!result && tryCount++ <10)
		{
		System.gc();
		result = f.delete();
		}
		return result;
	}


	private void check(String domain, String expected,String ipHex) {
		m_factory.setDomain(domain);
		m_factory.setIpAddress(ipHex); // 192.168.63.153
		String actual = m_factory.getNextId().toString();

		Assert.assertEquals(expected, actual);

		MessageId id = MessageId.parse(actual);

		Assert.assertEquals(domain, id.getDomain());
		Assert.assertEquals(ipHex, id.getIpAddressInHex());
	}

	@After
	public void clear() {
		m_factory.close();
		m_factory = null;
		//System.gc();
		cleanup();
	}

	@Test
	public void test() {
		String id = "UNKNOWN-c0a82050-376665-314";
		MessageId message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals(id, message.toString());

		id = "ARCH-UNKNOWN-c0a82050-376665-314";
		message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals("ARCH-UNKNOWN", message.getDomain());
		Assert.assertEquals(id, message.toString());

	}

	@Test(timeout = 500)
	public void test_performance() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("test_performance");

		for (int i = 0; i < 10000; i++) {
			f1.getNextId();
		}
		f1.close();
	}

	@Test
	public void testInit() throws IOException {
		m_factory.initialize("testInit");
	}

	@Test
	public void testMultithreads() throws IOException, InterruptedException {
		m_factory.initialize("testMultithreads");
		//Cat.enableMultiInstances();
		int count = 50;
		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch mainLatch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			Threads.forGroup("cat").start(new CreateMessageIdTask(i, latch, mainLatch));
		}
		latch.countDown();
		
		mainLatch.await();
		Assert.assertEquals(500000, m_factory.getIndex());
		
		//Cat.disableMultiInstances();
	}

	@Test 
	public void testNextId() throws Exception {
		m_factory.initialize("test1");
		String ipHex = m_factory.genIpHex();
	
		String prefix = "test1-"+ipHex+"-369535";
		check("test1", prefix+"-0",ipHex);
		check("test1", prefix+"-1",ipHex);
		check("test1", prefix+"-2",ipHex);
		check("test1", prefix+"-3",ipHex);

		m_timestamp = m_timestamp + MessageIdFactory.HOUR;
		ipHex="c0a83f99";
		check("domain1", "domain1-c0a83f99-369536-0",ipHex);
		check("domain1", "domain1-c0a83f99-369536-1",ipHex);
		check("domain1", "domain1-c0a83f99-369536-2",ipHex);

		m_timestamp = m_timestamp + MessageIdFactory.HOUR;
		check("domain1", "domain1-c0a83f99-369537-0",ipHex);
		check("domain1", "domain1-c0a83f99-369537-1",ipHex);
		check("domain1", "domain1-c0a83f99-369537-2",ipHex);
	}

	@Test
	public void testNextIdContinousIncrement() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("testNextIdContinousIncrement1");

		String id1 = f1.getNextId();
		String id2 = f1.getNextId();

		f1.close();

		MessageIdFactory f2 = new MessageIdFactory();

		f2.initialize("testNextIdContinousIncrement2");

		String id3 = f2.getNextId();
		String id4 = f2.getNextId();

		f2.close();

		Assert.assertEquals(false, id1.equals(id2));
		Assert.assertEquals(false, id3.equals(id4));

		Assert.assertEquals(false, id1.equals(id3));
		Assert.assertEquals(false, id2.equals(id4));
	}

	@Test
	public void testRpcServerId() throws IOException {
		m_factory.initialize("testRpcServerId");

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				String domain = "domain" + j;
				String nextId = m_factory.getNextId(domain);
				System.out.println(nextId);
			}
		}

		m_factory.saveMark();
	}

	@Test
	public void testRpcServerIdMultithreads() throws IOException, InterruptedException {
		m_factory.initialize("testRpcServerIdMultithreads");
		int count = 50;
		CountDownLatch latch = new CountDownLatch(count);
		CountDownLatch mainLatch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			Threads.forGroup("cat").start(new CreateMapIdTask(i, latch, mainLatch));

			latch.countDown();
		}
		mainLatch.await();

		for (int j = 0; j < 50; j++) {
			String domain = "domain_" + j;
			String id = m_factory.getNextId(domain);

			Assert.assertEquals(500000, MessageId.parse(id).getIndex());
		}

	}

	void toHexString(StringBuilder sb, long value) {
		int offset = sb.length();

		do {
			int index = (int) (value & 0x0F);

			sb.append(digits[index]);
			value >>>= 4;
		} while (value != 0);

		int len = sb.length();

		while (offset < len) {
			char ch1 = sb.charAt(offset);
			char ch2 = sb.charAt(len - 1);

			sb.setCharAt(offset, ch2);
			sb.setCharAt(len - 1, ch1);
			offset++;
			len--;
		}
	}

	public class CreateMapIdTask implements Task {

		private int m_thread;

		private CountDownLatch m_latch;

		private CountDownLatch m_mainLatch;

		public CreateMapIdTask(int thread, CountDownLatch latch, CountDownLatch mainLatch) {
			m_thread = thread;
			m_latch = latch;
			m_mainLatch = mainLatch;
		}

		@Override
		public String getName() {
			return "create-message-" + m_thread;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				for (int i = 0; i < 10000; i++) {
					for (int j = 0; j < 50; j++) {
						String domain = "domain_" + j;

						m_factory.getNextId(domain);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			m_mainLatch.countDown();
		}

		@Override
		public void shutdown() {
		}
	}

	public class CreateMessageIdTask implements Task {

		private int m_thread;

		private CountDownLatch m_latch;

		private CountDownLatch m_mainLatch;

		public CreateMessageIdTask(int thread, CountDownLatch latch, CountDownLatch mainLatch) {
			m_thread = thread;
			m_latch = latch;
			m_mainLatch = mainLatch;
		}

		@Override
		public String getName() {
			return "create-message-" + m_thread;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				m_factory.getTimestamp();
				Object last=null;
				for (int i = 0; i < 10000; i++) {
					String id = m_factory.getNextId();
					last = id;
					MessageId.parse(id).getIndex();
				}
				System.out.println("last:"+last+" i:"+m_thread);
			} catch (Exception e) {
				e.printStackTrace();
			}
			m_mainLatch.countDown();
		}

		@Override
		public void shutdown() {
		}
	}
}
