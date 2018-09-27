package com.dianping.cat.opensource;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class OpensourceTest {

	@Test
	public void testTransaction() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("JavaClient7", "Bucket_" + String.valueOf(i % 10));

			try {
				Thread.sleep(5);
				t.setDurationInMillis(calMills(i));
				t.setSuccessStatus();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				t.complete();
			}
		}
		Thread.sleep(10 * 1000L);
	}

	private long calMills(int i) {
		long mills = i % 10 * 100L;

		if (i >= 950) {
			mills += 10;
		}
		return mills;
	}

	@Test
	public void testEvent() throws Exception {
		//Transaction t = Cat.newTransaction("Parent3", "name22");

		for (int i = 0; i < 1000; i++) {
			Cat.logEvent("JavaClient13", "Bucket_");
			Thread.sleep(5);
		}
		//t.complete();
		Thread.sleep(10 * 1000L);
	}

}
