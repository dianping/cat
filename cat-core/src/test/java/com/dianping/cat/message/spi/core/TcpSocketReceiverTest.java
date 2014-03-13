package com.dianping.cat.message.spi.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.core.TcpSocketReceiver.DecodeMessageTask;

public class TcpSocketReceiverTest extends ComponentTestCase {

	private MockMessageTreeBuilder builder = new MockMessageTreeBuilder();

	@Test
	public void test() throws Exception {
		BlockingQueue<ChannelBuffer> queue = new LinkedBlockingQueue<ChannelBuffer>(1);
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		buf.writeInt(231);
		buf.writeBytes("asdf".getBytes());

		queue.add(buf);

		MockHandler handler = new MockHandler();
		DecodeMessageTask task = new TcpSocketReceiver().new DecodeMessageTask(0, queue, new MockCodec(), handler);

		task.handleMessage();

		handler.assertEqual(builder.build());

		TcpSocketReceiver receiver = lookup(TcpSocketReceiver.class);
		
		receiver.init();
		Assert.assertEquals(true, receiver.isActive());
	}

	public class MockHandler implements MessageHandler {
		private MessageTree m_message;

		@Override
		public void handle(MessageTree message) {
			m_message = message;
		}

		public void assertEqual(MessageTree expectedMessage) {
			Assert.assertEquals(expectedMessage.toString(), m_message.toString());
		}

	}

	public class MockCodec implements MessageCodec {

		@Override
		public MessageTree decode(ChannelBuffer buf) {
			return builder.build();
		}

		@Override
		public void decode(ChannelBuffer buf, MessageTree tree) {

		}

		@Override
		public void encode(MessageTree tree, ChannelBuffer buf) {
		}
	}
}
