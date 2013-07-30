package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestChildrenMany {

	@Test
	public void test() throws Exception {
		Transaction t = Cat.newTransaction("Check1", "name");
			Transaction t3 = Cat.newTransaction("Check2", "name");
			for (int i = 0; i < 1080; i++) {
				Transaction t4 = Cat.newTransaction("Check3", "name");
				t4.complete();
			}
			t3.complete();
		t.complete();
		Thread.sleep(1000);
	}
}
