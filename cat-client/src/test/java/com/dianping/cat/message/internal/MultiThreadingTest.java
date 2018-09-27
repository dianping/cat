package com.dianping.cat.message.internal;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class MultiThreadingTest {
	@After
	public void after() {
	}

	@Before
	public void before() {
		Cat.initialize(new File("/data/appdatas/cat/client.xml"));
	}

	@Test
	public void testForkedTransaction() throws Exception {
		Transaction t = Cat.newTransaction("ForkedRoot", "Root");
		ForkedTransaction t1 = Cat.newForkedTransaction("ForkedChild", "Child1");
		ForkedTransaction t2 = Cat.newForkedTransaction("ForkedChild", "Child2");

		Threads.forGroup().start(new TimedThread(t1, 500)); // will run away
		Threads.forGroup().start(new TimedThread(t2, 100)); // will be back in time

		TimeUnit.MILLISECONDS.sleep(200);

		t.setStatus(Message.SUCCESS);
		t.complete();
	}

	@Test
	public void testTaggedTransaction() throws Exception {
		Transaction t = Cat.newTransaction("TaggedRoot", "Root");
		Cat.newTaggedTransaction("TaggedChild", "Child1", "Tag1");
		Cat.newTaggedTransaction("TaggedChild", "Child2", "Tag2");

		Threads.forGroup().start(new TaggedThread(500, "Tag1"));
		Threads.forGroup().start(new TaggedThread(100, "Tag2"));

		TimeUnit.MILLISECONDS.sleep(200);

		t.setStatus(Message.SUCCESS);
		t.complete();
	}

	static class TaggedThread extends Thread {
		private int m_timeout;

		private String m_tag;

		public TaggedThread(int timeout, String tag) {
			m_timeout = timeout;
			m_tag = tag;
		}

		@Override
		public void run() {
			Transaction t = Cat.newTransaction("TaggedThread", m_tag);

			try {
				TimeUnit.MILLISECONDS.sleep(m_timeout);

				t.setStatus(Message.SUCCESS);
				Cat.getManager().bind(m_tag, "Child Tagged Thread");
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}
		}
	}

	static class TimedThread extends Thread {
		private ForkedTransaction m_transaction;

		private int m_timeout;

		public TimedThread(ForkedTransaction t, int timeout) {
			m_transaction = t;
			m_timeout = timeout;
		}

		@Override
		public void run() {
			m_transaction.fork();

			try {
				TimeUnit.MILLISECONDS.sleep(m_timeout);

				Cat.logEvent("TimedThread", "Timeout." + m_timeout);

				m_transaction.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
				m_transaction.setStatus(e);
			} finally {
				m_transaction.complete();
			}
		}
	}
}
