package com.dianping.cat.message.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;

public class InMemoryTransportTest {
	@Test
	public void test() throws InterruptedException, ExecutionException {
		final Transport transport = new InMemoryTransport();
		final int len = 1000;
		final StringBuilder sb = new StringBuilder(len * 4);
		ExecutorService pool = Executors.newFixedThreadPool(3);
		List<Future<?>> futures = new ArrayList<Future<?>>();

		futures.add(pool.submit(new Runnable() {
			@Override
			public void run() {
				transport.onMessage(new MockMessageHandler(sb));
			}
		}));
		futures.add(pool.submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < len; i++) {
					MockMessage m = new MockMessage();

					m.setName(String.valueOf(i));
					transport.send(m);
				}

				transport.shutdown();
			}
		}));

		for (Future<?> future : futures) {
			future.get();
		}
		pool.shutdown();

		Assert.assertEquals(2890, sb.length());
	}

	static class MockMessage implements Message {
		private String m_name;

		@Override
		public void addData(String keyValuePairs) {
		}

		@Override
		public void addData(String key, Object value) {
		}

		@Override
		public void complete() {
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public String getStatus() {
			return null;
		}

		@Override
		public long getTimestamp() {
			return 0;
		}

		@Override
		public String getType() {
			return null;
		}

		public void setName(String name) {
			m_name = name;
		}

		@Override
		public void setStatus(String status) {
		}

      @Override
      public void setStatus(Throwable e) {
      }
	}

	static class MockMessageHandler implements MessageHandler {
		private StringBuilder m_sb;

		public MockMessageHandler(StringBuilder sb) {
			m_sb = sb;
		}

		@Override
		public void handle(Message message) {
			m_sb.append(message.getName());
		}
	}
}
