package com.dianping.cat.message.spi.internal;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Bind;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.TcpSocketReceiver;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultMessageHandler extends ContainerHolder implements MessageHandler, Initializable, LogEnabled,
      Runnable {
	@Inject
	private MessageManager m_manager;

	@Inject
	private MessageConsumerRegistry m_registry;

	private MessageReceiver m_receiver;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(MessageTree tree) {
		List<MessageConsumer> consumers = m_registry.getConsumers();
		int size = consumers.size();
		
		for (int i = 0; i < size; i++) {
			MessageConsumer consumer = consumers.get(i);

			try {
				consumer.consume(tree);
			} catch (Exception e) {
				m_logger.error("Error when consuming message in " + consumer + "!", e);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_manager.getServerConfig();

		if (config == null) {
			// by default, no configuration needed in develop mode, all in memory
			m_receiver = lookup(MessageReceiver.class, "in-memory");
		} else {
			String mode = config.getMode();

			if ("server".equals(mode)) {
				TcpSocketReceiver receiver = (TcpSocketReceiver) lookup(MessageReceiver.class, "tcp-socket");
				Bind bind = config.getBind();

				receiver.setHost(bind.getIp());
				receiver.setPort(bind.getPort());
				receiver.initialize();

				m_receiver = receiver;
			} else if ("broker".equals(mode)) {
				throw new UnsupportedOperationException("Not implemented yet");
			} else {
				throw new IllegalArgumentException(String.format("Unsupported mode(%s) in message handler!", mode));
			}
		}
	}

	@Override
	public void run() {
		m_receiver.onMessage(this);
	}

	public void setRegistry(MessageConsumerRegistry registry) {
		m_registry = registry;
	}

	public void shutdown() {
		m_receiver.shutdown();
	}
}
