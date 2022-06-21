package com.dianping.cat.message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.MessageAssert.TransactionAssert;
import com.dianping.cat.message.context.TraceContextHelper;
import com.dianping.cat.message.context.MessageIdFactory;
import com.dianping.cat.message.internal.DefaultForkedTransaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.pipeline.MessageHandler;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

public class MessageTest extends ComponentTestCase {
	private static StringBuilder sb = new StringBuilder(1024);

	private static AtomicInteger s_index = new AtomicInteger();

	private static AtomicInteger s_count = new AtomicInteger();

	@After
	public void after() {
		s_count.set(0);
		s_index.set(0);
		sb.setLength(0);
		MessageAssert.reset();
	}

	@Before
	public void before() throws Exception {
		Cat.getBootstrap().testMode();

		context().registerComponent(MessageHandler.class, new MockMessageHandler());
		context().registerComponent(MessageIdFactory.class, new MockMessageIdFactory());
	}

	private void checkMessageIdUsed(int expected) throws InterruptedException {
		int count = 100;

		while (count > 0 && expected != s_count.get()) {
			TimeUnit.MILLISECONDS.sleep(5);
			count--;
			Thread.yield();
		}

		if (expected != s_count.get()) {
			Assert.fail(String.format("%s message ids should be used, but was %s!", expected, s_count.get()));
		}
	}

	//
	// @Test
	// public void testBulkEvent() {
	// MyBulkEvent e = Cat.newBulkEvent("type", "name");
	//
	// e.addCount(10, 3);
	// e.success();
	// e.complete();
	//
	// MessageAssert.event().type("type").name("name").success().complete() //
	// .data(SYSTEM.getKey(), "10,3");
	// }
	//
	// @Test
	// public void testBulkTransaction() {
	// MyBulkTransaction t = Cat.newBulkTransaction("type", "name");
	//
	// t.addDuration(3, 7, 12345);
	// t.success();
	// t.complete();
	//
	// MessageAssert.transaction().type("type").name("name").success().complete() //
	// .data(SYSTEM.getKey(), "3,7,12345");
	// }

	@Test
	public void testChainedTasks() throws InterruptedException {
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final ExecutorService pool = Executors.newFixedThreadPool(3);
		final CountDownLatch latch = new CountDownLatch(3);

		final Thread task3 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task3");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();
				} finally {
					forked.close();
				}

				latch.countDown();
			}
		};

		final Thread task2 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task2");

					Cat.logEvent("type", "name");

					pool.submit(task3);

					t.success();
					t.complete();
				} finally {
					forked.close();
				}

				latch.countDown();
			}
		};

		Thread task1 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task1");

					Cat.logEvent("type", "name");

					pool.submit(task2);
					t.success();
					t.complete();
				} finally {
					forked.close();
				}

				latch.countDown();
			}
		};

		pool.submit(task1);

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();
		pool.shutdown();

		checkMessageIdUsed(1);

		List<TransactionAssert> tas = MessageAssert.transaction().childTransaction(0).type("Forkable")
		      .childTransactions("Embedded");

		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			// each has one child transaction and one grand child event
			ta.childTransaction(0).childEvent(0);
			names.add(ta.childTransaction(0).transaction().getName());
		}

		Assert.assertEquals("[task1, task2, task3]", names.toString());
	}

	@Test
	public void testChainedTasks2() throws InterruptedException {
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final ExecutorService pool = Executors.newFixedThreadPool(3);
		final CountDownLatch latch0 = new CountDownLatch(1);
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(2);

		final Thread task3 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task3");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();
				} finally {
					forked.close();
				}

				latch2.countDown();
			}
		};

		final Thread task2 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task2");

					Cat.logEvent("type", "name");

					latch0.await();
					pool.submit(task3);

					t.success();
					t.complete();
				} catch (InterruptedException e) {
					// ignore it
				} finally {
					forked.close();
				}

				latch2.countDown();
			}
		};

		Thread task1 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = p.doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task1");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();

					pool.submit(task2);
				} finally {
					forked.close();
				}

				latch.countDown();
			}
		};

		pool.submit(task1);

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();
		latch0.countDown();

		latch2.await();
		pool.shutdown();

		checkMessageIdUsed(3);

		// main message
		TransactionAssert forkable = MessageAssert.tree("mock-7f000001-412057-0") //
		      .transaction().childTransaction(0).type("Forkable");
		List<TransactionAssert> tas = forkable.childTransactions();

		Assert.assertEquals(1, forkable.childTransactions("Embedded").size());
		Assert.assertEquals(2, forkable.childTransactions("Detached").size());
		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			if (ta.transaction().getType().equals("Embedded")) {
				// each has one child transaction and one grand child event
				ta.childTransaction(0).childEvent(0);
				names.add(ta.childTransaction(0).transaction().getName());
			} else { // Forked
				ta.noChild();
			}
		}

		Assert.assertEquals("[task1]", names.toString());

		// check for 2 forked messages
		MessageAssert.tree("mock-7f000001-412057-1").transaction().childTransaction(0).childEvent(0);
		MessageAssert.tree("mock-7f000001-412057-2").transaction().childTransaction(0).childEvent(0);
	}

	@Test
	public void testEvent() {
		Event e = Cat.newEvent("type", "name");

		e.success();
		e.complete();

		MessageAssert.event().type("type").name("name").success().complete();
	}

	@Test
	public void testExceptionDedup() {
		Transaction t = Cat.newTransaction("type", "name");
		Exception e1 = new Exception("e1");
		Exception e2 = new Exception("e2");

		Cat.logError(e1);
		Cat.logError(e1);

		Cat.logError(e2);
		Cat.logError(e2);
		Cat.logError(e2);

		t.success();
		t.complete();

		Assert.assertEquals(2, t.getChildren().size());
	}

	@Test
	public void testExceptionFormat() {
		Exception e = new Exception("message here");
		StringWriter sw = new StringWriter(1024);

		Cat.logError(e);
		e.printStackTrace(new PrintWriter(sw));

		MessageAssert.event().data(sw.toString());
	}

	@Test
	public void testForkAndDetachWithThread() throws InterruptedException {
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final CountDownLatch latch0 = new CountDownLatch(1);
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(2);

		for (int i = 0; i < 3; i++) {
			final int index = i;

			new Thread() {
				@Override
				public void run() {
					ForkedTransaction forked = p.doFork();

					try {
						Transaction t = Cat.newTransaction("type", "child" + index);

						Cat.logEvent("type", "name");

						if (index == 0) {
							t.success();
							t.complete();
						} else {
							try {
								latch0.await();
							} catch (InterruptedException e) {
								// ignore it
							}

							t.success();
							t.complete();
						}
					} finally {
						forked.close();
					}

					if (index == 0) {
						latch.countDown();
					} else {
						latch2.countDown();
					}
				}
			}.start();
		}

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();
		latch0.countDown();
		latch2.await();

		checkMessageIdUsed(3);

		// main message
		TransactionAssert forkable = MessageAssert.tree("mock-7f000001-412057-0") //
		      .transaction().childTransaction(0).type("Forkable");
		List<TransactionAssert> tas = forkable.childTransactions();

		Assert.assertEquals(1, forkable.childTransactions("Embedded").size());
		Assert.assertEquals(2, forkable.childTransactions("Detached").size());
		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			if (ta.transaction().getType().equals("Embedded")) {
				// each has one child transaction and one grand child event
				ta.childTransaction(0).childEvent(0);
				names.add(ta.childTransaction(0).transaction().getName());
			} else { // Forked
				ta.noChild();
			}
		}

		Assert.assertEquals("[child0]", names.toString());

		// check for 2 forked messages
		MessageAssert.tree("mock-7f000001-412057-1").transaction().childTransaction(0).childEvent(0);
		MessageAssert.tree("mock-7f000001-412057-2").transaction().childTransaction(0).childEvent(0);
	}

	@Test
	public void testForkAndDetachWithThreadPool() throws InterruptedException {
		int threads = 3;
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final CountDownLatch latch0 = new CountDownLatch(1);
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(2);
		ExecutorService pool = Executors.newFixedThreadPool(2);

		for (int i = 0; i < threads; i++) {
			final int index = i;

			pool.submit(new Thread() {
				@Override
				public void run() {
					ForkedTransaction forked = p.doFork();

					try {
						Transaction t = Cat.newTransaction("type", "child" + index);

						Cat.logEvent("type", "name");

						if (index == 0) {
							t.success();
							t.complete();
						} else {
							try {
								latch0.await();
							} catch (InterruptedException e) {
								// ignore it
							}

							t.success();
							t.complete();
						}
					} finally {
						forked.close();
					}

					if (index == 0) {
						latch.countDown();
					} else {
						latch2.countDown();
					}
				}
			});
		}

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();
		latch0.countDown();
		latch2.await();

		checkMessageIdUsed(3);

		// main message
		TransactionAssert forkable = MessageAssert.tree("mock-7f000001-412057-0") //
		      .transaction().childTransaction(0).type("Forkable");
		List<TransactionAssert> tas = forkable.childTransactions();

		Assert.assertEquals(1, forkable.childTransactions("Embedded").size());
		Assert.assertEquals(2, forkable.childTransactions("Detached").size());
		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			if (ta.transaction().getType().equals("Embedded")) {
				// each has one child transaction and one grand child event
				ta.childTransaction(0).childEvent(0);
				names.add(ta.childTransaction(0).transaction().getName());
			} else { // Forked
				ta.noChild();
			}
		}

		Assert.assertEquals("[child0]", names.toString());

		// check for 2 forked messages
		MessageAssert.tree("mock-7f000001-412057-1").transaction().childTransaction(0).childEvent(0);
		MessageAssert.tree("mock-7f000001-412057-2").transaction().childTransaction(0).childEvent(0);
	}

	@Test
	public void testForkAndJoinWithThread() throws InterruptedException {
		int threads = 3;
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final CountDownLatch latch = new CountDownLatch(threads);

		for (int i = 0; i < threads; i++) {
			final int index = i;

			new Thread() {
				@Override
				public void run() {
					ForkedTransaction forked = p.doFork();

					try {
						Transaction t = Cat.newTransaction("type", "child" + index);

						Cat.logEvent("type", "name");
						t.success();
						t.complete();
						latch.countDown();
					} finally {
						forked.close();
					}
				}
			}.start();
		}

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();

		checkMessageIdUsed(1);

		List<TransactionAssert> tas = MessageAssert.transaction().childTransaction(0).type("Forkable")
		      .childTransactions("Embedded");

		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			// each has one child transaction and one grand child event
			ta.childTransaction(0).childEvent(0);
			names.add(ta.childTransaction(0).transaction().getName());
		}

		Assert.assertEquals("[child0, child1, child2]", names.toString());
	}

	@Test
	public void testForkAndJoinWithThreadPool() throws InterruptedException {
		int threads = 3;
		Transaction t = Cat.newTransaction("type", "parent");
		final ForkableTransaction p = t.forFork();
		final CountDownLatch latch = new CountDownLatch(threads);
		ExecutorService pool = Executors.newFixedThreadPool(2);

		for (int i = 0; i < threads; i++) {
			final int index = i;

			pool.submit(new Thread() {
				@Override
				public void run() {
					ForkedTransaction forked = p.doFork();

					try {
						Transaction t = Cat.newTransaction("type", "child" + index);

						Cat.logEvent("type", "name");
						t.success();
						t.complete();
						latch.countDown();
					} finally {
						forked.close();
					}
				}
			});
		}

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();
		pool.shutdown();

		checkMessageIdUsed(1);

		List<TransactionAssert> tas = MessageAssert.transaction().childTransaction(0).type("Forkable")
		      .childTransactions("Embedded");

		Assert.assertEquals(3, tas.size());

		Set<String> names = new TreeSet<String>();

		for (TransactionAssert ta : tas) {
			// each has one child transaction and one grand child event
			ta.childTransaction(0).childEvent(0);
			names.add(ta.childTransaction(0).transaction().getName());
		}

		Assert.assertEquals("[child0, child1, child2]", names.toString());
	}

	@Test
	public void testMessageComplete() {
		Transaction t1 = Cat.newTransaction("type", "name");

		Event e1 = Cat.newEvent("type", "name");
		e1.success();
		e1.complete();

		Event e2 = Cat.newEvent("type", "name");
		e2.success();
		// not completed
		t1.addChild(e2);

		Transaction t2 = Cat.newTransaction("type", "name");
		t2.success();
		t2.complete();

		Transaction t3 = Cat.newTransaction("type", "name");
		t3.success();
		// not completed

		t1.success();
		t1.complete();

		TransactionAssert ta = MessageAssert.transaction().complete();
		ta.childEvent(0).complete();
		ta.childEvent(1).notComplete();
		ta.childTransaction(0).complete();
		ta.childTransaction(1).notComplete();
	}

	@Test
	public void testMessageData() {
		Transaction t1 = Cat.newTransaction("type", "name");
		t1.addData("keyValuePairs");
		t1.addData("key", "value");
		t1.success();
		t1.complete();
		MessageAssert.transaction().data("keyValuePairs").data("key", "value");

		Transaction t2 = Cat.newTransaction("type", "name");
		t2.addData("key1", "value1");
		t2.addData("key2", "value2");
		t2.addData("key3", "value3");
		t2.success();
		t2.complete();
		MessageAssert.transaction().data("key1", "value1").data("key2", "value2").data("key3", "value3");
	}

	@Test
	public void testMessageStatus() {
		Transaction t1 = Cat.newTransaction("type", "name");
		t1.complete();
		MessageAssert.transaction().status("unset");

		Transaction t2 = Cat.newTransaction("type", "name");
		t2.setStatus("transaction-status");
		t2.complete();
		MessageAssert.transaction().status("transaction-status");

		Transaction t3 = Cat.newTransaction("type", "name");
		t3.success();
		t3.complete();
		MessageAssert.transaction().success();

		Transaction t4 = Cat.newTransaction("type", "name");
		t4.setStatus(new RuntimeException());
		t4.complete();
		MessageAssert.transaction().status("java.lang.RuntimeException");

		Event e1 = Cat.newEvent("type", "name");
		e1.complete();
		MessageAssert.event().status("unset");

		Event e2 = Cat.newEvent("type", "name");
		e2.setStatus("event-status");
		e2.complete();
		MessageAssert.event().status("event-status");

		Event e3 = Cat.newEvent("type", "name");
		e3.success();
		e3.complete();
		MessageAssert.event().success();

		Event e4 = Cat.newEvent("type", "name");
		e4.setStatus(new RuntimeException());
		e4.complete();
		MessageAssert.event().status(RuntimeException.class.getName());

		Cat.logError(new RuntimeException());

		MessageAssert.event().status("ERROR");
	}

	@Test
	public void testMessageStatusOverwrite() {
		Transaction t1 = Cat.newTransaction("type", "name");

		t1.setStatus("error");
		t1.success();
		t1.complete();

		Assert.assertEquals(Message.SUCCESS, t1.getStatus());

		Transaction t2 = Cat.newTransaction("type", "name");

		t2.success();
		t2.setStatus("error");
		t2.complete();

		Assert.assertEquals("error", t2.getStatus());

		Transaction t3 = Cat.newTransaction("type", "name");

		t3.setStatus("unset");
		t3.success();
		t3.complete();

		Assert.assertEquals(Message.SUCCESS, t3.getStatus());
	}

	@Test
	public void testMessageTypeName() {
		Transaction t1 = Cat.newTransaction("transaction-type", "transaction-name");
		t1.success();
		t1.complete();
		MessageAssert.transaction().type("transaction-type").name("transaction-name");

		Transaction t2 = Cat.newTransaction("transaction-type", "transaction-name");
		t2.success();

		if (t2 instanceof DefaultTransaction) {
			((DefaultTransaction) t2).setType("transaction-type2"); // type could be changed
			((DefaultTransaction) t2).setName("transaction-name2"); // name could be changed
		}

		t2.complete();
		MessageAssert.transaction().type("transaction-type2").name("transaction-name2");

		Event e1 = Cat.newEvent("event-type", "event-name");
		e1.success();
		e1.complete();
		MessageAssert.event().type("event-type").name("event-name");
	}

	@Test
	public void testNestedTasks() throws InterruptedException {
		Transaction t = Cat.newTransaction("type", "parent");
		final AtomicReference<ForkableTransaction> forkable = new AtomicReference<ForkableTransaction>();
		final ExecutorService pool = Executors.newFixedThreadPool(3);
		final CountDownLatch latch = new CountDownLatch(1);

		forkable.set(t.forFork());

		final Thread task3 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = forkable.get().doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task3");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();
				} finally {
					forked.close();
				}

				latch.countDown();
			}
		};

		final Thread task2 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = forkable.get().doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task2");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();

					forkable.set(t.forFork());
					pool.submit(task3);
				} finally {
					forked.close();
				}
			}
		};

		Thread task1 = new Thread() {
			@Override
			public void run() {
				ForkedTransaction forked = forkable.get().doFork();

				try {
					Transaction t = Cat.newTransaction("type", "task1");

					Cat.logEvent("type", "name");

					t.success();
					t.complete();

					forkable.set(t.forFork());
					pool.submit(task2);
				} finally {
					forked.close();
				}
			}
		};

		pool.submit(task1);

		latch.await();
		Cat.logEvent("type", "name");
		t.success();
		t.complete();

		pool.shutdown();

		checkMessageIdUsed(3);

		// main message
		TransactionAssert ta = MessageAssert.tree("mock-7f000001-412057-0").transaction();

		for (int i = 0; i < 3; i++) {
			ta = ta.childTransaction(0).type("Forkable").childTransaction(0).type("Embedded").childTransaction(0);
		}

		// Assert.assertEquals("", sb.toString());
	}

	@Test
	public void testPeekTransaction() {
		Transaction t1 = Cat.newTransaction("type1", "name1");

		Cat.logEvent("event-type", "name1");

		// following lines to simulate manipulating the peek transaction
		// as if it's in another method
		Transaction p1 = TraceContextHelper.threadLocal().peekTransaction();

		Cat.newEvent("event-type", "name2");
		p1.addData("key", "value");

		Assert.assertEquals(t1.getType(), p1.getType());
		Assert.assertEquals(t1.getName(), p1.getName());

		Transaction t2 = Cat.newTransaction("type2", "name2");
		Transaction p2 = TraceContextHelper.threadLocal().peekTransaction();

		t1.success();
		t1.complete();

		Assert.assertEquals(t2.getType(), p2.getType());
		Assert.assertEquals(t2.getName(), p2.getName());

		MessageAssert.transaction().data("key", "value");
	}

	@Test
	public void testRemoteCallForClient() throws InterruptedException {
		Transaction t = Cat.newTransaction("ServiceCall", "A");
		MessageTree tree = TraceContextHelper.threadLocal().getMessageTreeWithMessageId();
		String rootMessageId = tree.getRootMessageId();
		String parentMessageId = tree.getMessageId();
		ForkedTransaction forked = new DefaultForkedTransaction(rootMessageId, parentMessageId);

		forked.setMessageId(Cat.createMessageId());
		t.addChild(forked);

		// more child transactions or events could be appended as well

		t.success();
		t.complete();

		// message id,parent message id,root message id should be passed
		// via HTTP headers or request context to server side
		// and server can name the message with passed message id

		checkMessageIdUsed(2);

		MessageAssert.header().messageId("mock-7f000001-412057-0");
		MessageAssert.transaction().childTransaction(0) //
		      .type("Forked").name(Thread.currentThread().getName()).data("#", "mock-7f000001-412057-1");
	}

	@Test
	public void testRemoteCallForServer() throws InterruptedException {
		// assume below the message ids are passed from client side
		String rootMessageId = TraceContextHelper.threadLocal().nextMessageId();
		String parentMessageId = TraceContextHelper.threadLocal().nextMessageId();
		String messageId = TraceContextHelper.threadLocal().nextMessageId();
		MessageTree tree = TraceContextHelper.threadLocal().getMessageTree();

		tree.setMessageId(messageId);
		tree.setParentMessageId(parentMessageId);
		tree.setRootMessageId(rootMessageId);

		Transaction t = Cat.newTransaction("Service", "A");
		Cat.logEvent("type", "name");
		t.success();
		t.complete();

		// message id,parent message id,root message id should be passed to server
		// via HTTP headers or request context

		checkMessageIdUsed(3);

		MessageAssert.header().rootMessageId("mock-7f000001-412057-0")//
		      .parentMessageId("mock-7f000001-412057-1") //
		      .messageId("mock-7f000001-412057-2");
	}

	// @Test
	// public void testTick() throws InterruptedException {
	// Transaction t = Cat.newTransaction("type", "name");
	//
	// for (int i = 0; i < 5; i++) {
	// TimeUnit.MILLISECONDS.sleep(i);
	//
	// Event e = Cat.newEvent("event-type", "event-name");
	// e.success();
	// e.complete();
	//
	// long deltaInMicros = MessageContextHelper.threadLocal().deltaTickTimeInMicros();
	// Trace trace = Cat.newTrace(MessageTreeHelper.STOP_WATCH, "label" + i);
	//
	// trace.addData(new DecimalFormat("0.0 ms").format(deltaInMicros / 1000.d));
	// trace.success();
	// trace.complete();
	// }
	//
	// t.success();
	// t.complete();
	//
	// TransactionAssert ta = MessageAssert.transaction();
	//
	// ta.childEvent(0).type("event-type").name("event-name").success().complete();
	// ta.childTrace(0).type(MessageTreeHelper.STOP_WATCH).name("label0").success().complete();
	//
	// ta.childEvent(4).type("event-type").name("event-name").success().complete();
	// ta.childTrace(4).type(MessageTreeHelper.STOP_WATCH).name("label4").success().complete();
	// }

	@Test
	public void testTransaction() {
		Transaction t = Cat.newTransaction("type", "name");

		Event e = Cat.newEvent("event-type", "event-name");
		e.success();
		e.complete();

		t.setStatus("status");
		t.complete();

		TransactionAssert ta = MessageAssert.transaction().type("type").name("name").status("status").complete();

		ta.childEvent(0).type("event-type").name("event-name").success().complete();
	}

	@Test
	public void testTransactionWithException() {
		Transaction t = Cat.newTransaction("type", "name");
		Exception ex = new Exception();

		Cat.logError(ex);
		Cat.logError(ex);
		t.setStatus(ex);
		t.complete();

		TransactionAssert ta = MessageAssert.transaction().type("type").name("name").status("java.lang.Exception")
		      .complete();

		Assert.assertEquals(1, ta.childEvents().size());
		ta.childEvent(0).type("Error").name("java.lang.Exception").status("ERROR").complete();
	}

	@Test
	public void testTransactionWithException2() {
		Transaction t = Cat.newTransaction("type", "name");
		Exception ex = new Exception();

		t.setStatus(ex);
		Cat.logError(ex);
		Cat.logError(ex);
		t.complete();

		TransactionAssert ta = MessageAssert.transaction().type("type").name("name").status("java.lang.Exception")
		      .complete();

		Assert.assertEquals(1, ta.childEvents().size());
		ta.childEvent(0).type("Error").name("java.lang.Exception").status("ERROR").complete();
	}

	@Test
	public void testTransactionWithStartAndEndTime() {
		Transaction t = Cat.newTransaction("type", "name");

		Cat.logEvent("event-type", "event-name");

		t.setStatus("status");
		t.complete(1514539976144L, 1514539977144L);

		TransactionAssert ta = MessageAssert.transaction().type("type").name("name").status("status").complete();

		ta.duration(1000L);
		ta.childEvent(0).type("event-type").name("event-name").success().complete();
	}

	private static class MockMessageHandler extends MessageHandlerAdaptor {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
			MessageAssert.newTree(tree);
			sb.append(tree);
		}
	}

	private static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		public String getNextId() {
			StringBuilder sb = new StringBuilder(32);

			sb.append("mock");
			sb.append('-');
			sb.append("7f000001");
			sb.append('-');
			sb.append(412057);
			sb.append('-');
			sb.append(s_index.getAndIncrement());

			s_count.incrementAndGet();
			return sb.toString();
		}
	}
}
