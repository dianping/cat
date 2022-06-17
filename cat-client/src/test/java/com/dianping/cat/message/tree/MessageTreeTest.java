package com.dianping.cat.message.tree;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.io.MessageTreePool;

public class MessageTreeTest extends ComponentTestCase {
	private MessageTree m_tree;

	@Test
	public void test() {
		context().registerComponent(MessageTreePool.class, new MockMessageTreePool());

		Cat.newTransaction("type", "name").success().complete();
		
		System.out.println(m_tree);
	}

	private class MockMessageTreePool implements MessageTreePool {
		@Override
		public void feed(MessageTree tree) {
			m_tree = tree;
		}

		@Override
		public MessageTree poll() throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}
	}
}
