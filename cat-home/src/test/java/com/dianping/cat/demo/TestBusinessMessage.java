package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestBusinessMessage {

	@Test
	public void test() throws Exception {
		while (true) {

			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "/index");
				Cat.logMetric("order", "quantity", i, "channel", "channel"+i % 5);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}
			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "/detail");
				Cat.logMetric("payment.pending", "amount", i, "channel","channel"+ i % 5);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}
			for (int i = 0; i < 500; i++) {
				Transaction t = Cat.newTransaction("URL", "/order/submitOrder");
				Cat.logMetric("payment.success", "amount", i, "channel", "channel"+i % 5);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}
			
			Thread.sleep(10000);
		}
	}

}
