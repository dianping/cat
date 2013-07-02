package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class TestABTestBusinessMessage {

	private static final String TuanGou = "TuanGouWeb";

	private static final String PayOrder = "PayOrder";

	@Test
	public void test() throws Exception {

			while (true) {
	         for (int i = 0; i < 1000; i++) {
		         Transaction t = Cat.newTransaction("URL", "/index");
		         String abtest = buildAbStr(i);

		         Cat.logEvent("URL", "ABTest", Event.SUCCESS, abtest);
		         ((DefaultMessageManager) Cat.getManager()).setMetricType(abtest);

		         MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
		         tree.setDomain(TuanGou);
		         t.complete();
	         }
	         for (int i = 0; i < 800; i++) {
		         Transaction t = Cat.newTransaction("URL", "/detail");
		         String abtest = buildAbStr(i);

		         Cat.logEvent("URL", "ABTest", Event.SUCCESS, abtest);
		         ((DefaultMessageManager) Cat.getManager()).setMetricType(abtest);

		         MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
		         tree.setDomain(TuanGou);
		         t.complete();
	         }
	         for (int i = 0; i < 500; i++) {
		         Transaction t = Cat.newTransaction("URL", "/order/submitOrder");
		         String abtest = buildAbStr(i);

		         Cat.logEvent("URL", "ABTest", Event.SUCCESS, abtest);
		         ((DefaultMessageManager) Cat.getManager()).setMetricType(abtest);
		         Cat.logMetricForCount("order");
		         Cat.logMetricForDuration("time", 500);
		         Cat.logMetricForSum("payment.success", i);
		         Cat.logMetricForSum("Order1", i);
		         Cat.logMetricForSum("Order2", i);
		         Cat.logMetricForSum("Order1-10", i);

		         MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
		         tree.setDomain(PayOrder);
		         t.complete();
	         }
	         Thread.sleep(1000);
         }

	}

	private String buildAbStr(int i) {
		int value = i % 3;
		if (value == 0) {
			return "1=ab:A&2=ab:A";
		} else if (value == 1) {
			return "1=ab:B&2=ab:B";
		} else {
			return "1=ab:C&2=ab:";
		}
	}
}
