package com.dianping.cat.demo;

import java.io.InputStream;

import org.junit.Test;
import org.unidal.helper.Urls;
import org.unidal.webres.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestCode {

	@Test
	public void testEncode() throws Exception {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("1#1", "1#1");

			t.complete();
		}

		Thread.sleep(1000);
	}

	@Test
	public void testTime() throws Exception {
		long time = System.currentTimeMillis() - TimeHelper.ONE_MINUTE * 5;
		String format = "http://localhost:2281/cat/r/monitor?timestamp=%s&group=db-%s&domain=test&key=myKey2&op=count";

		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 5; j++) {
				String url = String.format(format, time, "database" + j);
				InputStream in = Urls.forIO().readTimeout(1000).connectTimeout(1000).openStream(url);
				Files.forIO().readFrom(in, "utf-8");
			}
		}
	}

	@Test
	public void testEvent() throws Exception {
		for (int i = 0; i < 100; i++) {
			Event event = Cat.newEvent("fff", String.valueOf(i));

			event.setStatus(Event.SUCCESS);
			event.complete();
		}

		Thread.sleep(1000 * 1000);
	}

	public void logError(Throwable able) {
		Transaction t = Cat.newTransaction("Neocortex", "Error");

		Cat.logError(able);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain("NeoCortex");
		t.complete();
	}

	@Test
	public void test1() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			test();
		}

		Thread.sleep(1000 * 10);
	}

	public void test() throws InterruptedException {
		Transaction t = Cat.newTransaction("Neocortex", "function1");
		try {
			int a = functionA();

			if (a < 0) {
				Cat.logError(new RuntimeException("sdsf"));
			}

			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

			((DefaultMessageTree) tree).setDomain("NeoCortex");
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {

			Cat.logError(e);
			t.setStatus(e);

		} finally {
			t.complete();
		}

	}

	private int functionA() {

		return (int) (Math.random() * 100) - 50;
	}

}
