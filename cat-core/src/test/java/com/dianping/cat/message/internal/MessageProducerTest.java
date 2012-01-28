package com.dianping.cat.message.internal;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class MessageProducerTest extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		MessageManager manager = lookup(MessageManager.class);

		manager.initializeClient(null);
		manager.setup(null, null);
	}

	@After
	public void after() throws Exception {
		MessageManager manager = lookup(MessageManager.class);

		manager.reset();
	}

	@Test
	public void testNormal() throws Exception {
		MessageProducer producer = lookup(MessageProducer.class);
		InMemoryQueue queue = lookup(InMemoryQueue.class);
		Transaction t = producer.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			Thread.sleep(20);

			producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		Assert.assertEquals("One message should be in the queue.", 1, queue.size());

		MessageTree tree = queue.peek();
		Message m = tree.getMessage();

		Assert.assertTrue(Transaction.class.isAssignableFrom(m.getClass()));

		Transaction trans = (Transaction) m;

		Assert.assertEquals("URL", trans.getType());
		Assert.assertEquals("MyPage", trans.getName());
		Assert.assertEquals("0", trans.getStatus());
		Assert.assertTrue(trans.getDuration() > 0);
		Assert.assertEquals("k1=v1&k2=v2&k3=v3", trans.getData().toString());

		Assert.assertEquals(1, trans.getChildren().size());

		Message c = trans.getChildren().get(0);

		Assert.assertEquals("URL", c.getType());
		Assert.assertEquals("Payload", c.getName());
		Assert.assertEquals("0", c.getStatus());
		Assert.assertEquals("host=my-host&ip=127.0.0.1&agent=...", c.getData().toString());
	}
}
