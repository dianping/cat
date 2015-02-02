package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestCrossMessage {
	
	@Test
	public void testCross() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Call", "CallServiceMethod");

			Cat.logEvent("Call.server", "10.128.114.217:8080");
			Cat.logEvent("Call.app", "catserver1");
			Cat.logEvent("Call.port", "1000");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catweb01");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Call", "CallServiceMethod");

			Cat.logEvent("Call.server", "10.128.114.217:8081");
			Cat.logEvent("Call.app", "catserver2");
			Cat.logEvent("Call.port", "1000");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catweb01");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Call", "CallServiceMethod");

			Cat.logEvent("Call.server", "10.128.114.217:8080");
			Cat.logEvent("Call.app", "catserver1");
			Cat.logEvent("Call.port", "1111");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catweb02");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Call", "CallServiceMethod");

			Cat.logEvent("Call.server", "10.128.114.217:8081");
			Cat.logEvent("Call.app", "catserver2");
			Cat.logEvent("Call.port", "1111");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catweb02");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}

		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Service", "CallServiceMethod");

			Cat.logEvent("Service.client", "10.128.114.217:1000");
			Cat.logEvent("Service.app", "catweb01");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catserver1");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Service", "CallServiceMethod");

			Cat.logEvent("Service.client", "10.128.114.217:1111");
			Cat.logEvent("Service.app", "catweb02");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catserver1");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Service", "CallServiceMethod");

			Cat.logEvent("Service.client", "10.128.114.217:1000");
			Cat.logEvent("Service.app", "catweb01");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catserver2");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("Service", "CallServiceMethod");

			Cat.logEvent("Service.client", "10.128.114.217:1111");
			Cat.logEvent("Service.app", "catweb02");

			Cat.getManager().getThreadLocalMessageTree().setDomain("catserver2");
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}

		Thread.sleep(10000);
	}
}
