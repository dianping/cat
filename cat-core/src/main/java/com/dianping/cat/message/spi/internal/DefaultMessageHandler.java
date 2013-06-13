package com.dianping.cat.message.spi.internal;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageHandler extends ContainerHolder implements MessageHandler, LogEnabled {
	private MessageConsumerRegistry m_registry;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(MessageTree tree) {
		if (m_registry == null) {
			m_registry = lookup(MessageConsumerRegistry.class);
		}

		List<MessageConsumer> consumers = m_registry.getConsumers();
		int size = consumers.size();

		for (int i = 0; i < size; i++) {
			MessageConsumer consumer = consumers.get(i);

			try {
				consumer.consume(tree);
			} catch (Throwable e) {
				m_logger.error("Error when consuming message in " + consumer + "! tree: " + tree, e);
			}
		}
	}
}
