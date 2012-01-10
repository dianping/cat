package com.dianping.cat.message.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
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
	public void testOneToMany() throws Exception {
		final MessageReceiver receiver = lookup(MessageReceiver.class, "tcp-socket");
		int numSenders = 10;
		final int len = 1000;
		final StringBuilder sb = new StringBuilder(len);
		ExecutorService pool = Executors.newFixedThreadPool(3);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		final CountDownLatch forSender = new CountDownLatch(numSenders);
		final CountDownLatch forReceiver = new CountDownLatch(numSenders * len);

		receiver.initialize();
		receiver.onMessage(new MockMessageHandler(sb, forReceiver));

		final MessageSender[] senders = new MessageSender[numSenders];

		for (int i = 0; i < senders.length; i++) {
			senders[i] = lookup(MessageSender.class, "tcp-socket");
		}

		for (int i = 0; i < senders.length; i++) {
			final MessageSender sender = senders[i];

			futures.add(pool.submit(new Runnable() {
				@Override
				public void run() {
					sender.initialize();

					for (int i = 0; i < len; i++) {
						sender.send(new DefaultMessageTree());
					}

					forSender.countDown();
				}
			}));
		}

		forSender.await();
		forReceiver.await();

		receiver.shutdown();
		pool.shutdown();

		for (int i = 0; i < senders.length; i++) {
			senders[i].shutdown();
		}

		Assert.assertEquals(numSenders * len, sb.length());
	}

	@Test
	public void testOneToOne() throws Exception {
		final MessageSender sender = lookup(MessageSender.class, "tcp-socket");
		final MessageReceiver receiver = lookup(MessageReceiver.class, "tcp-socket");
		final int len = 1000;
		final StringBuilder sb = new StringBuilder(len);
		ExecutorService pool = Executors.newFixedThreadPool(3);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		final CountDownLatch forReceiver = new CountDownLatch(len);

		receiver.initialize();
		receiver.onMessage(new MockMessageHandler(sb, forReceiver));

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

		forReceiver.await();

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
		public void decode(ChannelBuffer buf, MessageTree tree) {
			// do nothing here
		}

		@Override
		public void encode(MessageTree tree, ChannelBuffer buf) {
			buf.writeInt(4);
			buf.writeBytes("mock".getBytes());
		}
	}

	static class MockMessageHandler implements MessageHandler {
		private StringBuilder m_sb;

		private CountDownLatch m_forReceiver;

		public MockMessageHandler(StringBuilder sb, CountDownLatch forReceiver) {
			m_sb = sb;
			m_forReceiver = forReceiver;
		}

		@Override
		public void handle(MessageTree tree) {
			synchronized (this) {
				m_sb.append('.');
				m_forReceiver.countDown();
			}
		}
	}
}
