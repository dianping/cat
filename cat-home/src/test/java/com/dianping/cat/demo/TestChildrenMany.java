package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestChildrenMany {

	@Test
	public void test() {
		Transaction t = Cat.newTransaction("type", "name");

		for (int i = 0; i < 1005; i++) {
			Cat.logEvent("type", "name");
		}

		t.complete();
	}
}
