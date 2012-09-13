package com.dianping.cat.message.spi.internal;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.TcpSocketReceiver;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Threads.Task;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultMessageHandler extends ContainerHolder implements MessageHandler, Initializable, LogEnabled, Task {
	@Inject
	private ServerConfigManager m_configManager;

	private MessageConsumerRegistry m_registry;

	private MessageReceiver m_receiver;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void handle(MessageTree tree) {
		if (m_registry == null) {
			m_registry = lookup(MessageConsumerRegistry.class);
		}

		List<MessageConsumer> consumers = m_registry.getConsumers();

		for (MessageConsumer consumer : consumers) {
			try {
				consumer.consume(tree);
			} catch (Throwable e) {
				m_logger.error("Error when consuming message in " + consumer + "! tree: " + tree, e);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		// TODO
		if (!m_configManager.isInitialized()) {
			// by default, no configuration needed in develop mode, all in memory
			m_receiver = lookup(MessageReceiver.class, "in-memory");
		} else {
			TcpSocketReceiver receiver = (TcpSocketReceiver) lookup(MessageReceiver.class, "tcp-socket");

			receiver.setHost(m_configManager.getBindHost());
			receiver.setPort(m_configManager.getBindPort());
			receiver.initialize();

			m_receiver = receiver;
		}
	}

	@Override
	public void run() {
		m_receiver.onMessage(this);
	}

	@Override
	public void shutdown() {
		m_receiver.shutdown();
	}
}
