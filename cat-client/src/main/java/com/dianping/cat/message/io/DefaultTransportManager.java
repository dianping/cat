package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Server;

public class DefaultTransportManager implements TransportManager, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private TcpSocketSender m_tcpSocketSender;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public MessageSender getSender() {
		return m_tcpSocketSender;
	}

	@Override
	public void initialize() throws InitializationException {
		List<Server> servers = m_configManager.getServers();

		if (!m_configManager.isCatEnabled()) {
			m_tcpSocketSender = null;
			m_logger.warn("CAT was DISABLED due to not initialized yet!");
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
			} else {
				m_tcpSocketSender.setServerAddresses(addresses);
				m_tcpSocketSender.initialize();
			}
		}
	}

}
