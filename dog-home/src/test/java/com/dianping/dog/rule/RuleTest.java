package com.dianping.dog.rule;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleContext;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventListenerRegistry;
import com.dianping.dog.event.EventType;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class RuleTest extends ComponentTestCase {
//	private static StringBuilder s_result = new StringBuilder();
//
//	@Test
//	public void test() throws Exception {
//		EventListenerRegistry registry = lookup(EventListenerRegistry.class);
//		EventDispatcher dispatcher = lookup(EventDispatcher.class);
//
//		registry.register(MockEventType.CONNECTION, new ConnectionEventListener(dispatcher));
//		registry.register(MockEventType.ALARM, new AlarmEventListener());
//
//		s_result.setLength(0);
//
//		dispatcher.dispatch(new ConnectionEvent(10));
//		dispatcher.dispatch(new ConnectionEvent(2));
//		dispatcher.dispatch(new ConnectionEvent(100));
//		dispatcher.dispatch(new ConnectionEvent(25));
//		dispatcher.dispatch(new ConnectionEvent(70));
//		dispatcher.dispatch(new ConnectionEvent(20));
//
//		Assert.assertEquals("2:100:70:", s_result.toString());
//	}
//
//	static class AlarmEvent implements Event {
//		private int m_connections;
//
//		public AlarmEvent(int connections) {
//			m_connections = connections;
//		}
//
//		public int getConnections() {
//			return m_connections;
//		}
//
//		@Override
//		public EventType getType() {
//			return MockEventType.ALARM;
//		}
//
//	}
//
//	static class AlarmEventListener implements EventListener<AlarmEvent> {
//		@Override
//		public void onEvent(AlarmEvent event) {
//			s_result.append(event.getConnections()).append(":");
//		}
//
//	}
//
//	static class ConnectionEvent implements Event {
//		private int m_connections;
//
//		public ConnectionEvent(int connections) {
//			m_connections = connections;
//		}
//
//		public int getConnections() {
//			return m_connections;
//		}
//
//		@Override
//		public EventType getType() {
//			return MockEventType.CONNECTION;
//		}
//
//	}
//
//	static class ConnectionEventListener extends AbstractRuleReactor<ConnectionEvent> {
//		public ConnectionEventListener(EventDispatcher dispatcher) {
//			setDispatcher(dispatcher);
//			setRule(ConnectionRule.INSTANCE);
//		}
//
//		@Override
//		protected Event createNextEvent(RuleContext ctx) {
//			return new AlarmEvent(getEvent(ctx).getConnections());
//		}
//
//		@Override
//		protected void prepare(RuleContext ctx, ConnectionEvent event) {
//		}
//
//	}
//
//	static enum ConnectionRule implements Rule {
//		INSTANCE;
//
//		@Override
//		public boolean apply(RuleContext ctx) {
//			int connections = ctx.getAttribute("connections");
//
//			return connections < 3 || connections > 30;
//		}
//
//		@Override
//		public String getName() {
//			return getClass().getSimpleName();
//		}
//	}
//
//	static enum MockEventType implements EventType {
//		CONNECTION, ALARM;
//	}
}
