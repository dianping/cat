package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Server;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

public class DefaultTransportManager extends ContainerHolder implements TransportManager, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	private MessageSender m_sender;

	private Logger m_logger;

	@Override
	public MessageSender getSender() {
		return m_sender;
	}

	@Override
	public void initialize() throws InitializationException {
		List<Server> servers = m_configManager.getServers();

		if (!m_configManager.isCatEnabled()) {
			m_sender = null;

			if (m_configManager.isInitialized()) {
				m_logger.warn("CAT was DISABLED explicitly!");
			} else {
				m_logger.warn("CAT was DISABLED due to not initialized yet!");
			}
		} else {
			List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

			for (Server server : servers) {
				if (server.isEnabled()) {
					addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
				}
			}

			m_logger.info("Remote CAT servers: " + addresses);

			if (addresses.isEmpty()) {
				throw new RuntimeException("All servers in configuration are disabled!\r\n" + servers);
			} else if (addresses.size() == 1) {
				TcpSocketSender sender = (TcpSocketSender) lookup(MessageSender.class, "tcp-socket");

				sender.setServerAddress(addresses.get(0));
				sender.initialize();
				m_sender = sender;
			} else {
				TcpSocketHierarchySender sender = (TcpSocketHierarchySender) lookup(MessageSender.class,
				      "tcp-socket-hierarchy");

				sender.setServerAddresses(addresses);
				sender.initialize();
				m_sender = sender;
			}
		}
	}

	public void setSender(MessageSender sender) {
		m_sender = sender;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
