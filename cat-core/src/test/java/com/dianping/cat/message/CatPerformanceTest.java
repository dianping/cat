package com.dianping.cat.message;

import static com.dianping.cat.message.Message.SUCCESS;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class CatPerformanceTest {

	private int count = 100000;

	private int threadNumber = 4;

	private static int error = 0;
	
	@Before
	public void before(){
		Cat.initialize(new File("/data/appdatas/cat/client.xml"));
	}
	
	@After
	public void after(){
		Cat.reset();
	}
	
	private void creatInternal(){
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL2", "WebPage");
		String id1 = cat.createMessageId();
		String id2 = cat.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}
	
	private void creatOneTransaction() {
		Cat.setup("");
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL4", "WebPage");
		String id1 = cat.createMessageId();
		String id2 = cat.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			creatInternal();
			cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
		Cat.reset();
	}

	@Ignore
	@Test
	public void test() {
		Cat.initialize(null);
		long time = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			creatOneTransaction();
		}
		long endtime = System.currentTimeMillis();

		System.out.println("avg:" + (double) (endtime - time) / (double) count + "ms");
		Cat.destroy();
	}

	@Test
	public void testManyThread() throws IOException {
		System.out.println("press any key to continue...");
		System.in.read();
		
		CountDownLatch start = new CountDownLatch(threadNumber);
		CountDownLatch end = new CountDownLatch(threadNumber);
		for (int i = 0; i < threadNumber; i++) {
			TestThread thread = new TestThread(start, end);
			thread.start();
			start.countDown();
		}
		try {
			end.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done with errors: " + error);
	}

	class TestThread extends Thread {

		CountDownLatch m_end;

		CountDownLatch m_latch;

		public TestThread(CountDownLatch latch, CountDownLatch end) {
			m_latch = latch;
			m_end = end;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long time = 0;
			long endtime = 0;
			time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
					creatOneTransaction();
			}
			endtime = System.currentTimeMillis();

			System.out.println(Thread.currentThread().getName() + " avg: " + (double) (endtime - time) / (double) count
			      + "ms");
			m_end.countDown();
		}
	}

}
