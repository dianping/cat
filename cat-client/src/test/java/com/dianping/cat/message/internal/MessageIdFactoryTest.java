package com.dianping.cat.message.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

public class MessageIdFactoryTest {
	private long m_timestamp = 1330327814748L;

	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private MessageIdFactory m_factory = new MessageIdFactory() {
		@Override
		protected long getTimestamp() {
			return m_timestamp / MessageIdFactory.HOUR;
		}
	};

	@Before
	public void before() {
		final List<String> paths = new ArrayList<String>();
		String base = "/data/appdatas/cat/";
		Scanners.forDir().scan(new File(base), new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (path.indexOf("mark") > -1) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});

		for (String path : paths) {
			boolean result = new File(base, path).delete();
			System.err.println("delete " + path + " " + result);
		}
	}

	private void check(String domain, String expected) {
		m_factory.setDomain(domain);
		m_factory.setIpAddress("c0a83f99"); // 192.168.63.153

		String actual = m_factory.getNextId().toString();

		Assert.assertEquals(expected, actual);

		MessageId id = MessageId.parse(actual);

		Assert.assertEquals(domain, id.getDomain());
		Assert.assertEquals("c0a83f99", id.getIpAddressInHex());
	}

	@After
	public void clear() {
		m_factory.close();
	}

	@Test
	public void test() {
		String id = "UNKNOWN-c0a82050-376665-314";
		MessageId message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals(id, message.toString());

		id = "ARCH-UNKNOWN-c0a82050-376665-314";
		message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals("ARCH-UNKNOWN", message.getDomain());
		Assert.assertEquals(id, message.toString());

	}

	@Test(timeout = 500)
	public void test_performance() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("test");

		for (int i = 0; i < 10000; i++) {
			f1.getNextId();
		}
	}

	@Test
	public void testInit() throws IOException {
		m_factory.initialize("test");
	}

	@Test
	public void testMultithreads() throws IOException, InterruptedException {
		m_factory.initialize("test");
		int count = 50;
		CountDownLatch latch = new CountDownLatch(count);
		CountDownLatch mainLatch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			Threads.forGroup("cat").start(new CreateMessageIdTask(i, latch, mainLatch));

			latch.countDown();
		}

		mainLatch.await();
		
		Assert.assertEquals(500000, MessageId.parse(m_factory.getNextId()).getIndex());
	}

	@Test
	public void testNextId() throws Exception {
		m_factory.initialize("test");

		check("domain1", "domain1-c0a83f99-369535-0");
		check("domain1", "domain1-c0a83f99-369535-1");
		check("domain1", "domain1-c0a83f99-369535-2");
		check("domain1", "domain1-c0a83f99-369535-3");

		m_timestamp = m_timestamp +MessageIdFactory.HOUR;
		check("domain1", "domain1-c0a83f99-369536-0");
		check("domain1", "domain1-c0a83f99-369536-1");
		check("domain1", "domain1-c0a83f99-369536-2");

		m_timestamp = m_timestamp +MessageIdFactory.HOUR;
		check("domain1", "domain1-c0a83f99-369537-0");
		check("domain1", "domain1-c0a83f99-369537-1");
		check("domain1", "domain1-c0a83f99-369537-2");
	}

	@Test
	public void testNextIdContinousIncrement() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("test");

		String id1 = f1.getNextId();
		String id2 = f1.getNextId();

		f1.close();

		MessageIdFactory f2 = new MessageIdFactory();

		f2.initialize("test");

		String id3 = f2.getNextId();
		String id4 = f2.getNextId();

		// f2.close();

		Assert.assertEquals(false, id1.equals(id2));
		Assert.assertEquals(false, id3.equals(id4));

		Assert.assertEquals(false, id1.equals(id3));
		Assert.assertEquals(false, id2.equals(id4));
	}

	@Test
	public void testRpcServerId() throws IOException {
		m_factory.initialize("test");

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				String domain = "domain" + j;
				System.out.println(m_factory.getNextId(domain));
			}
		}

		m_factory.saveMark();
	}

	@Test
	public void testRpcServerIdMultithreads() throws IOException, InterruptedException {
		m_factory.initialize("test");
		int count = 50;
		CountDownLatch latch = new CountDownLatch(count);
		CountDownLatch mainLatch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			Threads.forGroup("cat").start(new CreateMapIdTask(i, latch, mainLatch));

			latch.countDown();
		}
		mainLatch.await();

		for (int j = 0; j < 50; j++) {
			String domain = "domain_" + j;
			String id = m_factory.getNextId(domain);

			Assert.assertEquals(500000, MessageId.parse(id).getIndex());
		}

	}

	void toHexString(StringBuilder sb, long value) {
		int offset = sb.length();

		do {
			int index = (int) (value & 0x0F);

			sb.append(digits[index]);
			value >>>= 4;
		} while (value != 0);

		int len = sb.length();

		while (offset < len) {
			char ch1 = sb.charAt(offset);
			char ch2 = sb.charAt(len - 1);

			sb.setCharAt(offset, ch2);
			sb.setCharAt(len - 1, ch1);
			offset++;
			len--;
		}
	}

	public class CreateMapIdTask implements Task {

		private int m_thread;

		private CountDownLatch m_latch;

		private CountDownLatch m_mainLatch;

		public CreateMapIdTask(int thread, CountDownLatch latch, CountDownLatch mainLatch) {
			m_thread = thread;
			m_latch = latch;
			m_mainLatch = mainLatch;
		}

		@Override
		public String getName() {
			return "create-message-" + m_thread;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				for (int i = 0; i < 10000; i++) {
					for (int j = 0; j < 50; j++) {
						String domain = "domain_" + j;

						m_factory.getNextId(domain);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			m_mainLatch.countDown();
		}

		@Override
		public void shutdown() {
		}
	}

	public class CreateMessageIdTask implements Task {

		private int m_thread;

		private CountDownLatch m_latch;

		private CountDownLatch m_mainLatch;

		public CreateMessageIdTask(int thread, CountDownLatch latch, CountDownLatch mainLatch) {
			m_thread = thread;
			m_latch = latch;
			m_mainLatch = mainLatch;
		}

		@Override
		public String getName() {
			return "create-message-" + m_thread;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				for (int i = 0; i < 10000; i++) {
					String id = m_factory.getNextId();

					MessageId.parse(id).getIndex();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			m_mainLatch.countDown();
		}

		@Override
		public void shutdown() {
		}
	}
}
