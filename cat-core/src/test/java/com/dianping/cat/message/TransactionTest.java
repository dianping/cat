package com.dianping.cat.message;

import org.junit.Test;

import com.dianping.cat.Cat;

public class TransactionTest {
	public static MessageFactory CAT = Cat.getFactory();

	@Test
	public void testNormal() {
		Transaction t = CAT.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			Thread.sleep(30);

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}
}
