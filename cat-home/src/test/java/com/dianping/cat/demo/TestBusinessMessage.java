package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestBusinessMessage {
	private static final String TuanGou = "TuanGouWeb";

	private static final String PayOrder = "PayOrder";

	@Test
	public void test() throws Exception {
		while (true) {

			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "/index");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.addData("channel=channel" + i % 5);

				DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
				tree.setDomain(TuanGou);
				t.complete();
			}

			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "/detail");
				DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();

				tree.setDomain(TuanGou);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}

			for (int i = 0; i < 500; i++) {
				Transaction t = Cat.newTransaction("URL", "/order/submitOrder");
				DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();

				tree.setDomain(PayOrder);
				Cat.logMetric("order", "quantity", 1, "channel", "channel" + i % 5);
				Cat.logMetric("payment.pending", "amount", i, "channel", "channel" + i % 5);
				Cat.logMetric("payment.success", "amount", i, "channel", "channel" + i % 5);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}
			
			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "t");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.complete();
			}
			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "e");
				t.complete();
			}
			for (int i = 0; i < 500; i++) {
				Transaction t = Cat.newTransaction("URL", "home");
				Cat.logMetric("order", "quantity", 1, "channel", "channel" + i % 5);
				Cat.logMetric("payment.pending", "amount", i, "channel", "channel" + i % 5);
				Cat.logMetric("payment.success", "amount", i, "channel", "channel" + i % 5);
				t.complete();
			}


			Thread.sleep(1000);
		}
	}
	
	@Test
	public void test2() throws Exception {
		while (true) {

			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "/index");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.addData("channel=channel" + i % 5);

				t.complete();
			}
			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "/detail");
				t.addData("channel=channel" + i % 5);
				t.complete();
			}
			for (int i = 0; i < 500; i++) {
				Transaction t = Cat.newTransaction("URL", "/order/submitOrder");
				Cat.logMetric("order", "quantity", 1, "channel", "channel" + i % 5);
				Cat.logMetric("payment.pending", "amount", i, "channel", "channel" + i % 5);
				Cat.logMetric("payment.success", "amount", i, "channel", "channel" + i % 5);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}

			Thread.sleep(1000);
			break;
		}
	}

}
