package com.dianping.cat.message.internal;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class MessageProducerTest extends ComponentTestCase {
	@Test
	public void testNormal() throws Exception {
		MessageProducer producer = lookup(MessageProducer.class);
		InMemoryQueue queue = lookup(InMemoryQueue.class);
		MessageCodec codec = lookup(MessageCodec.class, "plain-text");
		Transaction t = producer.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			Thread.sleep(30);

			producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		Assert.assertEquals("One message should be in the queue.", 1, queue.size());

		MessageTree tree = queue.peek();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.encode(tree, buf);
		Assert.assertEquals("...", buf.toString(Charset.forName("utf-8")));
	}
}
