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

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestSendMessage {

	@Test
	public void sendException() throws Exception {
		for (int i = 0; i < 10; i++) {
			Transaction t = Cat.newTransaction("Midas", "XXName");
			try {
				t.setStatus("Fail");

				DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();

				tree.setDomain("rs-mapi-web");
			} catch (Exception e) {
				t.setStatus(Transaction.SUCCESS);
				Cat.logError(e);
				throw e;
			} finally {
				t.complete();
			}
		}
		Thread.sleep(10000);
	}

	@Test
	public void sendSendUrlErrorMessage() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("URL", "Test");

			Event e1 = Cat.newEvent("test2", "success");
			e1.addData("_count", 100);

			Event e2 = Cat.newEvent("test2", "fail");
			e2.addData("_count", 100);

			t.addData("key and value");
			t.setStatus(new NullPointerException());
			t.complete();
		}
		Thread.sleep(10000);
	}

	@Test
	public void sendSendCallErrorMessage() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("Call", "Test");

			t.addData("key and value");
			t.setStatus(new NullPointerException());
			t.complete();

		}
		Thread.sleep(1000);
	}

	@Test
	public void sendSendSqlErrorMessage() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("SQL", "Test");

			t.addData("key and value");
			t.setStatus(new NullPointerException());
			t.complete();
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendMessage() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("Test", "Test");

			t.addData("key and value");
			t.complete();

		}
		Thread.sleep(1000);
	}

	@Test
	public void sendError() throws Exception {
		for (int i = 0; i < 100; i++) {
			Cat.getProducer().logError(new NullPointerException());
			Cat.getProducer().logError(new OutOfMemoryError());
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendEvent() throws Exception {
		for (int i = 0; i < 100; i++) {
			Event t = Cat.getProducer().newEvent("Test", "Test");

			t.addData("key and value");
			t.complete();
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendMetric() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("Type", "Name");

			Cat.logMetric("name", "key1", "value1", "key2", "value2");

			t.complete();
		}

		Thread.sleep(1000);
	}

	@Test
	public void sentHackPigenTransaction() throws Exception {
		for (int i = 0; i < 200; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.7.24:8080");
			Cat.getProducer().logEvent("RemoteCall", "Pigeon", Message.SUCCESS, "MessageID");
			t.addData("key and value");

			Thread.sleep(1);
			Cat.getManager().getThreadLocalMessageTree().setDomain("Pigeon");
			Cat.getManager().getThreadLocalMessageTree().setMessageId("Cat-c0a81a38-374214-1203");
			t.complete();
		}
	}

	@Test
	public void sendPigeonClientTransaction() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.64." + i + ":2280");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.64.11:2280");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		for (int i = 0; i < 200; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.7.24:8080");
			Cat.getProducer().logEvent("RemoteCall", "Test", Message.SUCCESS, "MessageID");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}

		for (int i = 0; i < 300; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.7.39:8080");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		Thread.sleep(100);
	}

	@Test
	public void sendPigeonServerTransaction() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method6");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t.addData("key and value");

			Thread.sleep(51);
			t.complete();
		}
		for (int i = 0; i < 200; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method8");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.20");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}

		for (int i = 0; i < 300; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method5");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.231");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		Thread.sleep(100);
	}

	@Test
	public void sendCacheTransaction() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("Cache.kvdb", "Method6");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t.addData("key and value");

			Thread.sleep(11);
			Transaction t2 = Cat.getProducer().newTransaction("Cache.local", "Method");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t2.addData("key and value");

			Thread.sleep(11);
			t2.complete();
			t.complete();
		}
	}

	@Test
	public void sendLongCacheTransaction() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("Cache.kvdb", "Method6");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t.addData("key and value");

			Thread.sleep(11);
			Transaction t2 = Cat.getProducer().newTransaction("Cache.local", "Method");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t2.addData("key and value");

			Thread.sleep(11);
			t2.complete();
			t.complete();
		}
	}

	@Test
	public void sendLongURLTransaction() throws Exception {
		for (int i = 0; i < 10; i++) {
			Transaction t = Cat.getProducer().newTransaction("URL", "Method6");
			t.addData("key and value");
			Thread.sleep(60);
			t.complete();
		}
	}

	@Test
	public void sendLongSQLTransaction() throws Exception {
		for (int i = 0; i < 10; i++) {
			Transaction t = Cat.getProducer().newTransaction("SQL", "Method6");
			t.addData("key and value");
			Thread.sleep(102);
			t.complete();
		}
	}

	@Test
	public void sendCacheTransactionWithMissed() throws Exception {
		for (int i = 0; i < 130; i++) {
			Transaction t = Cat.getProducer().newTransaction("Cache.kvdb", "Method" + i % 10);
			Cat.getProducer().newEvent("Cache.kvdb", "Method" + i % 10 + ":missed");
			t.addData("key and value");

			Transaction t2 = Cat.getProducer().newTransaction("Cache.web", "Method" + i % 10);
			Cat.getProducer().newEvent("Cache.web", "Method" + i % 10 + ":missed");
			t2.addData("key and value");
			Thread.sleep(2);
			t2.complete();
			t.complete();

			Transaction t3 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t3.addData("key and value");
			Thread.sleep(3);
			t3.complete();
		}

		Transaction t2 = Cat.getProducer().newTransaction("Cache.web", "Method");
		t2.addData("key and value");
		Thread.sleep(2);
		t2.complete();
		Thread.sleep(1000);
	}

	@Test
	public void sendMaxMessage() throws Exception {
		long time = System.currentTimeMillis();
		int i = 10;

		while (i > 0) {
			i++;
			Transaction total = Cat.newTransaction("cat", "Test");
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
			t5.addData("key and value");
			t5.setStatus(Message.SUCCESS);
			t5.complete();

			Transaction t6 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t6.addData("key and value");
			t6.setStatus(Message.SUCCESS);
			t6.complete();

			Transaction t7 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t7.addData("key and value");
			t7.setStatus(Message.SUCCESS);
			t7.complete();

			Transaction t8 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t8.addData("key and value");
			t8.setStatus(Message.SUCCESS);
			t8.complete();

			Transaction t9 = Cat.getProducer().newTransaction("Cache.memcached", "Method" + i % 10);
			t9.addData("key and value");
			t9.setStatus(Message.SUCCESS);
			t9.complete();
			t.complete();

			total.setStatus(Transaction.SUCCESS);
			t.complete();

			if (i % 10000 == 0) {
				long duration = System.currentTimeMillis() - time;
				System.out.println("[" + duration + "ms]" + "[total]" + i + "[每秒" + i / duration * 1000 + "]");
			}
		}
		Thread.sleep(10 * 1000);
	}

	@Test
	public void sendSqlTransaction() throws Exception {
		for (int k = 0; k < 5; k++) {
			for (int i = 0; i < 100; i++) {
				Transaction t = Cat.getProducer().newTransaction("SQL", "User.select" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Select").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t.addData("select * from hostinfo");
				t.setStatus(Message.SUCCESS);
				t.complete();

				Transaction t2 = Cat.getProducer().newTransaction("SQL", "User.insert" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Update").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t2.addData("update * from hostinfo");
				t2.complete();

				Transaction t3 = Cat.getProducer().newTransaction("SQL", "User.delete" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Delete").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t3.addData("delete * from hostinfo");
				t3.setStatus(Message.SUCCESS);
				t3.complete();
			}
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendOtherDomainSqlTransaction() throws Exception {
		for (int k = 0; k < 5; k++) {
			for (int i = 0; i < 100; i++) {
				Transaction t = Cat.getProducer().newTransaction("SQL", "User.select" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Select").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t.addData("select * from hostinfo");
				t.setStatus(Message.SUCCESS);
				Cat.getManager().getThreadLocalMessageTree().setDomain("CatDemo");
				t.complete();

				Transaction t2 = Cat.getProducer().newTransaction("SQL", "User.insert" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Update").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t2.addData("update * from hostinfo");
				Cat.getManager().getThreadLocalMessageTree().setDomain("CatDemo");
				t2.complete();

				Transaction t3 = Cat.getProducer().newTransaction("SQL", "User.delete" + i % 10);
				Cat.getProducer().newEvent("SQL.Method", "Delete").setStatus(Message.SUCCESS);
				Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + k)
										.setStatus(Message.SUCCESS);
				t3.addData("delete * from hostinfo");
				t3.setStatus(Message.SUCCESS);
				Cat.getManager().getThreadLocalMessageTree().setDomain("CatDemo");
				t3.complete();
			}
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendDependencyTransaction() throws Exception {
		int size = 10;
		for (int i = 0; i < size; i++) {
			Transaction t = Cat.getProducer().newTransaction("SQL", "User.select" + i % 10);
			Cat.getProducer().newEvent("SQL.Method", "Select").setStatus(Message.SUCCESS);
			Cat.getProducer().newEvent("SQL.Database", "jdbc:mysql://192.168.7.43:3306/database" + i % 4)
									.setStatus(Message.SUCCESS);
			t.addData("select * from hostinfo");
			t.setStatus(Message.SUCCESS);
			t.complete();
		}

		for (int i = 0; i < size; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", "192.168.64.11:2280");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}

		for (int i = 0; i < size; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method6");
			Cat.getProducer().newEvent("PigeonService.client", "192.168.7.77");
			t.addData("key and value");

			Thread.sleep(51);
			t.complete();
		}
	}

	@Test
	public void sendTraceInfo() throws Exception {

		for (int i = 0; i < 10; i++) {
			Transaction t = Cat.newTransaction("Trace", "Test" + i);
			try {
				Cat.logTrace("Trace", "Info");
				Cat.logTrace("Trace", "Dubug", Trace.SUCCESS, "sss");
				Trace trace = Cat.newTrace("Trace", "Error");

				trace.setStatus(Trace.SUCCESS);
				trace.addData("errorTrace");
				t.setStatus("Fail");
			} catch (Exception e) {
				t.setStatus(Transaction.SUCCESS);
				Cat.logError(e);
				throw e;
			} finally {
				t.complete();
			}
		}
		Thread.sleep(10000);
	}
}