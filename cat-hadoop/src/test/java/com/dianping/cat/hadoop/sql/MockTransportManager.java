package com.dianping.cat.hadoop.sql;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class MockTransportManager implements TransportManager {
	private MockMessageSender m_sender = new MockMessageSender();

	@Override
	public MessageSender getSender() {
		return m_sender;
	}

	public MessageQueue getQueue() {
		return m_sender.getQueue();
	}

	static class MockMessageSender implements MessageSender {
		private DefaultMessageQueue m_queue = new DefaultMessageQueue();

		public MockMessageSender() {
			initialize();
		}

		public MessageQueue getQueue() {
			return m_queue;
		}

		@Override
		public void initialize() {
			try {
				m_queue.initialize();
			} catch (InitializationException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void send(MessageTree tree) {
			m_queue.offer(tree);
		}

		@Override
		public void shutdown() {
		}
	}
}
