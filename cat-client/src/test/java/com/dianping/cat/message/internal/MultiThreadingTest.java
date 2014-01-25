package com.dianping.cat.message.internal;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class MultiThreadingTest {
	@Test
	public void testForkedTransaction() throws Exception {
		Cat.initialize(new File("/data/appdatas/cat/client.xml"));
		Cat.setup(null);

		Transaction t = Cat.newTransaction("RootType", "RootName");
		ForkedTransaction d1 = Cat.newForkedTransaction("ForkedType", "Child1");
		ForkedTransaction d2 = Cat.newForkedTransaction("ForkedType", "Child2");

		Threads.forGroup().start(new MockThread(d1, 500)); // will run away
		Threads.forGroup().start(new MockThread(d2, 100)); // will be back in time

		TimeUnit.MILLISECONDS.sleep(200);

		t.setStatus(Message.SUCCESS);
		t.complete();

		Cat.reset();
	}

	static class MockThread extends Thread {
		private ForkedTransaction m_transaction;

		private int m_timeout;

		public MockThread(ForkedTransaction t, int timeout) {
			m_transaction = t;
			m_timeout = timeout;
		}

		@Override
		public void run() {
			m_transaction.fork();

			try {
				TimeUnit.MILLISECONDS.sleep(m_timeout);

				Cat.logEvent("MockType", "MockName." + m_timeout);

				m_transaction.setStatus(Message.SUCCESS);
			} catch (InterruptedException e) {
				m_transaction.setStatus(e);
			} finally {
				m_transaction.complete();
			}
		}
	}
}
