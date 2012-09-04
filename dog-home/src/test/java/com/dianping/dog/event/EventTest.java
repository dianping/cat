package com.dianping.dog.event;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class EventTest extends ComponentTestCase {
	private static StringBuilder s_result = new StringBuilder();

	@Test
	public void test() throws Exception {
		EventListenerRegistry registry = lookup(EventListenerRegistry.class);

		registry.register(MockEventType.TYPE1, MockEventListener.INSTANCE);

		EventDispatcher dispatcher = lookup(EventDispatcher.class);

		s_result.setLength(0);

		for (int i = 0; i < 10; i++) {
			dispatcher.dispatch(new MockEvent(i));
		}

		Assert.assertEquals("0:1:2:3:4:5:6:7:8:9:", s_result.toString());
	}

	static enum MockEventType implements EventType {
		TYPE1;
	}

	static enum MockEventListener implements EventListener<MockEvent> {
		INSTANCE;

		@Override
		public void onEvent(MockEvent event) {
			s_result.append(event.getIndex()).append(':');
		}

	}

	static class MockEvent implements Event {
		private int m_index;

		public MockEvent(int index) {
			m_index = index;
		}

		public int getIndex() {
			return m_index;
		}

		@Override
		public EventType getType() {
			return MockEventType.TYPE1;
		}

	}
}
