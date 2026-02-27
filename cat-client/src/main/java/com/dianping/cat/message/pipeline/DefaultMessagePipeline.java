package com.dianping.cat.message.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureManager;

// Component
public class DefaultMessagePipeline extends MessageHandlerAdaptor implements MessagePipeline, Initializable {
	// Inject
	private ConfigureManager m_configureManager;

	// Inject
	private Logger m_logger;

	// Inject
	private List<MessageHandler> m_handlers = new ArrayList<MessageHandler>();

	private MessageHandler m_tail = new TailHandler();

	@Override
	public void addLast(MessageHandler handler) {
		m_handlers.add(handler);
	}

	@Override
	public MessageHandlerContext headContext(Object message) {
		return new Context(new Source(message), -1);
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
		m_logger = ctx.lookup(Logger.class);

		List<MessageHandler> handlers = ctx.lookupList(MessageHandler.class);

		for (MessageHandler handler : handlers) {
			m_handlers.add(handler);
		}

		Collections.sort(m_handlers, new Comparator<MessageHandler>() {
			@Override
			public int compare(MessageHandler h1, MessageHandler h2) {
				return h1.getOrder() - h2.getOrder();
			}
		});
	}

	private class Context implements MessageHandlerContext {
		private MessageSource m_source;

		private int m_index;

		public Context(MessageSource source, int index) {
			m_source = source;
			m_index = index;
		}

		@Override
		public void fireMessage(Object msg) {
			Context next = next();

			next.handler().handleMessage(next, msg);
		}

		@Override
		public ConfigureManager getConfigureManager() {
			return m_configureManager;
		}

		@Override
		public Logger getLogger() {
			return m_logger;
		}

		@Override
		public MessageHandler handler() {
			if (m_index >= 0 && m_index < m_handlers.size()) {
				return m_handlers.get(m_index);
			}

			return m_tail;
		}

		private Context next() {
			return new Context(m_source, m_index + 1);
		}

		@Override
		public MessagePipeline pipeline() {
			return DefaultMessagePipeline.this;
		}

		@Override
		public MessageSource source() {
			return m_source;
		}
	}

	private class Source implements MessageSource {
		private Object m_message;

		public Source(Object message) {
			m_message = message;
		}

		@Override
		public Object getMessage() {
			return m_message;
		}

		@Override
		public MessagePipeline pipeline() {
			return DefaultMessagePipeline.this;
		}
	}

	private class TailHandler implements MessageHandler {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public void handleMessage(MessageHandlerContext ctx, Object msg) {
			// do nothing here
		}
	}
}
