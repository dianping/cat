package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class TestMaxMessage {

	@Test
	public void testSend() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.getProducer().newTransaction("CatTest", "CatTest" + i % 10);
			t.setStatus(Message.SUCCESS);
			Cat.getProducer().newEvent("Cache.kvdb", "Method" + i % 10 + ":missed");
			t.addData("key and value");
			t.complete();
		}
		Thread.sleep(10 * 100);
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
}