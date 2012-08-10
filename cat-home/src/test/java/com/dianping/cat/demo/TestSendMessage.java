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
		for (int i = 0; i < 180; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", ":30.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		for (int i = 0; i < 80; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", ":10.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}

		for (int i = 0; i < 280; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonCall", "Method3");
			Cat.getProducer().newEvent("PigeonCall.server", ":20.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		Thread.sleep(1000);
	}
	
	@Test
	public void sendPigeonServerTransaction() throws Exception {
		for (int i = 0; i < 180; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method1");
			Cat.getProducer().newEvent("PigeonCall.client", "30.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		for (int i = 0; i < 80; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method2");
			Cat.getProducer().newEvent("PigeonCall.client", "10.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}

		for (int i = 0; i < 280; i++) {
			Transaction t = Cat.getProducer().newTransaction("PigeonService", "Method4");
			Cat.getProducer().newEvent("PigeonCall.client", "20.12.6.1");
			t.addData("key and value");

			Thread.sleep(1);
			t.complete();
		}
		Thread.sleep(1000);
	}
}