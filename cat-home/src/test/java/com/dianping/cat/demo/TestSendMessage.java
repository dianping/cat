package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class TestSendMessage {

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
	public void sendEvent() throws Exception {
		for (int i = 0; i < 100; i++) {
			Event t = Cat.getProducer().newEvent("Test", "Test");

			t.addData("key and value");
			t.complete();
		}
		Thread.sleep(1000);
	}

	@Test
	public void sendPigeonClientTransaction() throws Exception {
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
}