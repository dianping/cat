package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestChildrenMany {

	@Test
	public void test() throws Exception {
		Transaction t = Cat.newTransaction("XXXX", "name");

		Transaction t2 = Cat.newTransaction("Transaction2", "name2");

		for (int i = 0; i < 888; i++) {

			Transaction t3 = Cat.newTransaction("Transaction3", "name");
			for (int j = 0; j < 999; j++) {
				Cat.logEvent("Event2", "name3");
			}
			t3.complete();
		}
		for (int i = 0; i < 1111; i++) {
			Cat.logEvent("Event1", "name");
		}
		t2.complete();
		t.complete();
		Thread.sleep(1000);
	}
}
