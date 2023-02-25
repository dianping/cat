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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class TestMaxMessage {

	@Test
	public void testSend() throws Exception {
		for (int i = 0; i < 10000; i++) {
			Transaction t = Cat.getProducer().newTransaction("CatTest", "CatTest" + i % 10);
			t.setStatus(Message.SUCCESS);
			Cat.getProducer().newEvent("Cache.kvdb", "Method" + i % 10 + ":missed");
			Cat.logError(new NullPointerException());
			t.addData("key and value");
			t.complete();
		}
		Thread.sleep(10 * 1000);
	}

	@Test
	public void sendMaxMessage() throws Exception {
		long time = System.currentTimeMillis();
		int i = 10;

		while (i > 0) {
			i++;
			Transaction total = Cat.newTransaction("Test", "Test");
			Transaction t = Cat.getProducer().newTransaction("Cache.kvdb", "Method" + i % 10);
			t.setStatus(Message.SUCCESS);
			Cat.getProducer().newEvent("Cache.kvdb", "Method" + i % 10 + ":missed");
			t.addData("key and value");

			Transaction t2 = Cat.getProducer().newTransaction("Cache.web", "Method" + i % 10);
			Cat.getProducer().newEvent("Cache.web", "Method" + i % 10 + ":missed");
			t2.addData("key and value");
			t2.setStatus(Message.SUCCESS);
			t2.complete();

			Transaction t3 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t3.addData("key and value");
			t3.setStatus(Message.SUCCESS);
			t3.complete();

			Transaction t4 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t4.addData("key and value");
			t4.setStatus(Message.SUCCESS);
			t4.complete();

			Transaction t5 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			Transaction t6 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t6.addData("key and value");
			t6.setStatus(Message.SUCCESS);
			t6.complete();

			Transaction t9 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			Transaction t7 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t7.addData("key and value");
			t7.setStatus(Message.SUCCESS);
			t7.complete();

			Transaction t8 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t8.addData("key and value");
			t8.setStatus(Message.SUCCESS);
			t8.complete();

			t9.addData("key and value");
			t9.setStatus(Message.SUCCESS);
			t9.complete();

			t5.addData("key and value");
			t5.setStatus(Message.SUCCESS);
			t5.complete();

			MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
			String messageId = tree.getMessageId();

			String[] ids = messageId.split("-");
			String ip6 = ids[1];

			String newMessageId = messageId.replaceAll(ip6, ip6.substring(0, ip6.length() - 1) + "" + i % 3);
			if (i % 3 == 1) {
				newMessageId = newMessageId.replaceAll("cat", "Cat1");
			} else if (i % 3 == 2) {
				newMessageId = newMessageId.replaceAll("cat", "Cat2");
			} else if (i % 3 == 0) {
				newMessageId = newMessageId.replaceAll("cat", "Cat0");
			}
			tree.setMessageId(newMessageId);
			t.complete();

			total.setStatus(Transaction.SUCCESS);
			total.complete();

			if (i % 10000 == 0) {
				long duration = System.currentTimeMillis() - time;
				System.out.println("[" + duration + "ms]" + "[total]" + i + "[每秒" + i / duration * 1000 + "]");
			}

		}
		Thread.sleep(10 * 1000);
	}

	@Test
	public void testThread() throws InterruptedException {
		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
		Threads.forGroup("f").start(new ThreadTest(map));
		Thread.sleep(TimeHelper.ONE_SECOND);
		int index = 0;

		synchronized (map) {
			for (Entry<String, String> entry : map.entrySet()) {
				System.out.println("index:" + index + " " + entry.getKey() + " " + entry.getValue());
				Thread.sleep(25);
				index++;
			}
		}

	}

	public class ThreadTest implements Task {

		ConcurrentHashMap<String, String> m_map;

		public ThreadTest(ConcurrentHashMap<String, String> map) {
			m_map = map;
		}

		@Override
		public void run() {
			for (int i = 0; i < 1000; i++) {
				m_map.put(String.valueOf(i), String.valueOf(i));
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public String getName() {
			return "cat";
		}

		@Override
		public void shutdown() {
		}

	}

}