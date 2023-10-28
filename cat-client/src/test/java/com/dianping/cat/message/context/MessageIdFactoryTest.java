package com.dianping.cat.message.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.support.Files;
import com.dianping.cat.support.Threads;

public class MessageIdFactoryTest extends ComponentTestCase {
	/**
	 * Run it multiple times in console to simulate multiple processes scenario,
	 * 
	 * to ensure multiple processes of same application working well in same one box.
	 */
	public static void main(String... args) throws Exception {
		String type = args.length > 0 ? args[0] : "master";
		String arg0 = args.length > 1 ? args[1] : null;
		MockApplication app = new MockApplication();

		if (type.equals("master")) {
			if (arg0 == null) {
				System.err.println("Options: master <processes>");
				System.exit(1);
			}

			int processes = Integer.parseInt(arg0);

			app.handleMaster(processes);
		} else if (type.equals("slave")) {
			app.handleSlave();
		} else {
			System.err.println("Options: [master|slave] <args>");
			System.exit(1);
		}
	}

	@Test
	public void testDefaultDomain() throws IOException {
		File baseDir = new File("target/mark");

		new File(baseDir, "default-domain.mark").delete();

		MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-domain");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("default-domain-c0a81f9e-403215-%s", i), factory.getNextId());
		}
	}

	@Test
	public void testGivenDomain() throws IOException {
		File baseDir = new File("target/mark");

		new File(baseDir, "given-domain.mark").delete();

		MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-domain");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("given-domain-c0a81f9e-403215-%s", i), factory.getNextId("given-domain"));
		}
	}

	@Test
	public void testDefaultDomainInParallel() throws Exception {
		File baseDir = new File("target/mark");

		new File(baseDir, "default-parallel.mark").delete();

		final MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-parallel");
		final Set<String> ids = Collections.synchronizedSet(new HashSet<String>());
		int threads = 100;
		final int messagesPerThread = 1234;
		ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threads);

		for (int thread = 0; thread < threads; thread++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < messagesPerThread; i++) {
						ids.add(factory.getNextId());
					}
				}
			});
		}

		pool.shutdown();
		boolean finished = pool.awaitTermination(60, TimeUnit.SECONDS);

		if (finished) {
			int total = threads * messagesPerThread;

			Assert.assertEquals("Not all threads completed in time.", total, ids.size());
      Assert.assertTrue(ids.contains(String.format("default-parallel-c0a81f9e-403215-%s", total - 1)));
			Assert.assertEquals(String.format("default-parallel-c0a81f9e-403215-%s", total), factory.getNextId());
		} else {
			Assert.fail("Threads did not finish in 60 seconds");
		}
	}

	@Test
	public void testGivenDomainInParallel() throws Exception {
		File baseDir = new File("target/mark");

		new File(baseDir, "given-parallel.mark").delete();

		final MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-parallel");
		final Set<String> ids = Collections.synchronizedSet(new HashSet<String>());
		int threads = 100;
		final int messagesPerThread = 1234;
		ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threads);

		for (int thread = 0; thread < threads; thread++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < messagesPerThread; i++) {
						ids.add(factory.getNextId("given-parallel"));
					}
				}
			});
		}

		pool.shutdown();
		pool.awaitTermination(2000, TimeUnit.MILLISECONDS);

		int total = threads * messagesPerThread;

		Assert.assertEquals("Not all threads completed in time.", total, ids.size());
		Assert.assertEquals(true, ids.contains(String.format("given-parallel-c0a81f9e-403215-%s", total - 1)));
		Assert.assertEquals(String.format("given-parallel-c0a81f9e-403215-%s", total),
		      factory.getNextId("given-parallel"));
	}

	@Test
	public void testDefaultDomainResume() throws IOException {
		File baseDir = new File("target/mark");

		new File(baseDir, "default-resume.mark").delete();

		// first round
		MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-resume");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("default-resume-c0a81f9e-403215-%s", i), factory.getNextId());
		}

		factory.close();

		// simulate when cat is stopped and started again
		factory = new MockMessageIdFactory(baseDir, "default-resume");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("default-resume-c0a81f9e-403215-%s", 100 + i), factory.getNextId());
		}
	}

	@Test
	public void testGivenDomainResume() throws IOException {
		File baseDir = new File("target/mark");

		new File(baseDir, "given-resume.mark").delete();

		// first round
		MessageIdFactory factory = new MockMessageIdFactory(baseDir, "default-resume");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("given-resume-c0a81f9e-403215-%s", i), factory.getNextId("given-resume"));
		}

		factory.close();

		// simulate when cat is stopped and started again
		factory = new MockMessageIdFactory(baseDir, "default-resume");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("given-resume-c0a81f9e-403215-%s", 100 + i),
			      factory.getNextId("given-resume"));
		}
	}

	public static class MockApplication {
		private File m_baseDir = new File("target/mark");

		public void handleMaster(int size) throws Exception {
			Files.forDir().delete(new File(m_baseDir, "multiple.mark"));

			final AtomicBoolean enabled = new AtomicBoolean(true);
			final ConcurrentMap<String, String> set = new ConcurrentHashMap<String, String>(1024);
			ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", size);
			final List<Process> processes = new ArrayList<Process>();

			for (int i = 0; i < size; i++) {
				String classpath = System.getProperty("java.class.path");
				String command = String.format("java -cp %s %s slave", classpath, MessageIdFactoryTest.class.getName());
				Process process = Runtime.getRuntime().exec(command);

				processes.add(process);
			}

			for (int i = 0; i < size; i++) {
				final InputStream in = processes.get(i).getInputStream();

				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(in));

							while (true) {
								String line = reader.readLine();

								if (line == null) {
									break;
								} else if (set.containsKey(line)) {
									System.out.println("Message ID conflicting found: " + line);
								} else {
									set.put(line, line);

									if (set.size() % 50000 == 0) {
										System.out.println("size:" + set.size());
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

			System.out.println("Press any key to stop ...");
			System.in.read();

			for (Process process : processes) {
				process.getOutputStream().close();
			}

			enabled.set(false);
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		}

		public void handleSlave() throws Exception {
			int threads = 10;
			final MockMessageIdFactory builder = new MockMessageIdFactory(m_baseDir, "multiple");
			final AtomicBoolean enabled = new AtomicBoolean(true);
			ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threads);

			for (int i = 0; i < threads; i++) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							while (enabled.get()) {
								System.out.println(builder.getNextId());

								TimeUnit.MILLISECONDS.sleep(1);
							}
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				});
			}

			System.in.read();

			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
	}

	private static class MockMessageIdFactory extends MessageIdFactory {
		private MockMessageIdFactory(File baseDir, String domain) {
			super.initialize(baseDir, domain);
		}

		@Override
		protected int getBatchSize() {
			return 10;
		}

		@Override
		protected long getHour() {
			return 403215;
		}

		@Override
		protected String getIpAddress() {
			return "c0a81f9e";
		}
	}
}