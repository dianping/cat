package com.dianping.cat;

import org.junit.Test;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class CatTest {
	@Test
	public void testWithoutInitialize() throws InterruptedException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);
	}
}
