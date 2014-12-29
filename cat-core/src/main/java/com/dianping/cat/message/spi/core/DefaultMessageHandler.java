package com.dianping.cat.message.spi.core;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageHandler implements MessageHandler, LogEnabled {
	@Inject
	private MessageConsumer m_consumer;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(MessageTree tree) {

		try {
			m_consumer.consume(tree);
		} catch (Throwable e) {
			m_logger.error("Error when consuming message in " + m_consumer + "! tree: " + tree, e);
		}
	}
}
