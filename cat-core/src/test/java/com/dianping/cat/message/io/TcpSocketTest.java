package com.dianping.cat.message.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class TcpSocketTest extends ComponentTestCase {
	@Test
	public void testOneToOne() throws Exception {
		final MessageSender sender = lookup(MessageSender.class, "tcp-socket");
		final MessageReceiver receiver = lookup(MessageReceiver.class, "tcp-socket");
		final int len = 1000;
		final StringBuilder sb = new StringBuilder(len);
		ExecutorService pool = Executors.newFixedThreadPool(3);
		List<Future<?>> futures = new ArrayList<Future<?>>();

		futures.add(pool.submit(new Runnable() {
			@Override
			public void run() {
				receiver.initialize();
				receiver.onMessage(new MockMessageHandler(sb));
			}
		}));
		futures.add(pool.submit(new Runnable() {
			@Override
			public void run() {
				sender.initialize();

				for (int i = 0; i < len; i++) {
					sender.send(new DefaultMessageTree());
				}
			}
		}));

		for (Future<?> future : futures) {
			future.get();
		}

		Thread.sleep(100);

		pool.shutdown();
		receiver.shutdown();
		sender.shutdown();

		Assert.assertEquals(len, sb.length());
	}

	static class MockMessage extends AbstractMessage {
		public MockMessage() {
			super(null, null);
		}

		@Override
		public void complete() {
		}
	}

	public static class MockMessageCodec implements MessageCodec {
		@Override
		public MessageTree decode(byte[] bytes) {
			return new DefaultMessageTree();
		}

		@Override
		public byte[] encode(MessageTree message) {
			return "mock".getBytes();
		}
	}

	static class MockMessageHandler implements MessageHandler {
		private StringBuilder m_sb;

		public MockMessageHandler(StringBuilder sb) {
			m_sb = sb;
		}

		@Override
		public void handle(MessageTree tree) {
			m_sb.append('.');
		}
	}
}
