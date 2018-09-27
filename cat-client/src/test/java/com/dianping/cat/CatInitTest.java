package com.dianping.cat;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.Transaction;

public class CatInitTest {

	@Test
	public void testInitCat() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(100);
		for (int i = 0; i < 50; i++) {
			Threads.forGroup("cat").start(new InitTask(latch));
			latch.countDown();
		}
		for (int i = 0; i < 50; i++) {

			Threads.forGroup("cat").start(new InitWithJobTask(latch));
			latch.countDown();
		}

		Thread.sleep(50 * 1000);
	}

	public static class InitTask implements Task {

		private CountDownLatch m_latch;

		public InitTask(CountDownLatch latch) {
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Transaction t = Cat.newTransaction("test", "test");

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t.complete();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {

		}
	}
	
	public static class InitWithJobTask implements Task {

		private CountDownLatch m_latch;

		public InitWithJobTask(CountDownLatch latch) {
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Cat.initializeByDomain("cat", "127.0.0.1","127.0.0.2");
			
			Transaction t = Cat.newTransaction("test", "test");

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t.complete();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {

		}
	}

}
