package com.dianping.cat.message.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Stack;

import junit.framework.Assert;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Files;
import org.unidal.helper.Reflects;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.message.CatTestCase;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

@RunWith(JUnit4.class)
public class MessageProducerTest extends CatTestCase {
	private Queue<MessageTree> m_queue;

	@BeforeClass
	public static void beforeClass() throws IOException {
		ClientConfig clientConfig = new ClientConfig();

		clientConfig.setMode("client");
		clientConfig.addDomain(new Domain("Test").setEnabled(true));

		File configFile = new File("target/client.xml").getCanonicalFile();

		configFile.getParentFile().mkdirs();

		Files.forIO().writeTo(configFile, clientConfig.toString());

		Cat.destroy();
		Cat.initialize(configFile);
	}

	@Before
	public void before() throws Exception {
		TransportManager manager = Cat.lookup(TransportManager.class);
		Initializable queue = Reflects.forField().getDeclaredFieldValue(manager.getSender().getClass(), "m_queue",
		      manager.getSender());

		queue.initialize();
		m_queue = Reflects.forField().getDeclaredFieldValue(queue.getClass(), "m_queue", queue);
	}

	@Test
	public void testNormal() throws Exception {
		MessageProducer producer = Cat.getProducer();
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

		// please stop CAT server when you run this test case
		Assert.assertEquals("One message should be in the queue.", 1, m_queue.size());

		MessageTree tree = m_queue.poll();
		Message m = tree.getMessage();

		Assert.assertTrue(Transaction.class.isAssignableFrom(m.getClass()));

		Transaction trans = (Transaction) m;

		Assert.assertEquals("URL", trans.getType());
		Assert.assertEquals("MyPage", trans.getName());
		Assert.assertEquals("0", trans.getStatus());
		Assert.assertTrue(trans.getDurationInMillis() > 0);
		Assert.assertEquals("k1=v1&k2=v2&k3=v3", trans.getData().toString());

		Assert.assertEquals(1, trans.getChildren().size());

		Message c = trans.getChildren().get(0);

		Assert.assertEquals("URL", c.getType());
		Assert.assertEquals("Payload", c.getName());
		Assert.assertEquals("0", c.getStatus());
		Assert.assertEquals("host=my-host&ip=127.0.0.1&agent=...", c.getData().toString());
	}

	@Test
	public void testNested() throws Exception {
		Stack<Transaction> stack = new Stack<Transaction>();

		for (int i = 0; i < 10; i++) {
			Transaction t = Cat.getProducer().newTransaction("Test", "TestName");

			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			stack.push(t);
		}

		while (!stack.isEmpty()) {
			Transaction t = stack.pop();

			t.setStatus(Message.SUCCESS);
			t.complete();
		}

		// please stop CAT server when you run this test case
		Assert.assertEquals("One message should be in the queue.", 1, m_queue.size());

		MessageTree tree = m_queue.poll();

		MessageCodec codec = Cat.lookup(MessageCodec.class, "plain-text");
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(4 * 1024);

		codec.encode(tree, buf);

		buf.readInt();
		MessageTree tree2 = codec.decode(buf);

		Assert.assertEquals(tree.toString(), tree2.toString());
	}
}
