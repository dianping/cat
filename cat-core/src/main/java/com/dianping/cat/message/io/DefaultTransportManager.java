package com.dianping.cat.message.io;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.message.spi.MessageManager;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultTransportManager extends ContainerHolder implements TransportManager, Initializable {
	@Inject
	private MessageManager m_manager;

	private MessageSender m_sender;

	@Override
	public MessageSender getSender() {
		if (m_sender == null) {
			throw new RuntimeException("Server mode only, no sender is provided!");
		}

		return m_sender;
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_manager.getClientConfig();

		if (config == null) {
			// by default, no configuration needed in develop mode, all in memory
			m_sender = lookup(MessageSender.class, "in-memory");
		} else {
			String mode = config.getMode();

			if ("client".equals(mode)) {
				List<Server> servers = config.getServers();
				int size = servers.size();

				if (size == 1) {
					TcpSocketSender sender = (TcpSocketSender) lookup(MessageSender.class, "tcp-socket");
					Server server = servers.get(0);

					sender.setHost(server.getIp());
					sender.setPort(server.getPort());
					sender.initialize();

					m_sender = sender;
				} else if (size == 0) {
					m_sender = lookup(MessageSender.class, "in-memory");
				} else {
					throw new UnsupportedOperationException("Not implemented yet");
				}
			} else {
				throw new IllegalArgumentException(String.format(
				      "Only mode(client) was supported in transport manager, but was mode(%s)!", mode));
			}
		}
	}

	public void setMessageManager(MessageManager manager) {
		m_manager = manager;
	}
}
