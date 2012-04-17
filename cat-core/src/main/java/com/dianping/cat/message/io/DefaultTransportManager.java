package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.spi.MessageManager;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultTransportManager extends ContainerHolder implements TransportManager, Initializable {
	@Inject
	private MessageManager m_manager;

	private MessageSender m_sender;

	public void setSender(MessageSender sender) {
		this.m_sender = sender;
	}

	@Override
	public MessageSender getSender() {
		if (m_sender == null) {
			throw new RuntimeException("Server mode only, no sender is provided!");
		}

		return m_sender;
	}

	@Override
	public void initialize() throws InitializationException {
		// disable thread renaming of Netty
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		ClientConfig config = m_manager.getClientConfig();

		if (config == null) {
			// by default, no configuration needed in develop mode, all in memory
			m_sender = lookup(MessageSender.class, "in-memory");
		} else {
			String mode = config.getMode();

			if ("client".equals(mode)) {
				List<Server> servers = config.getServers();
				int size = servers.size();

				if (size == 0 || !config.isEnabled()) {
					m_sender = lookup(MessageSender.class, "in-memory");
				} else {
					List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

					for (Server server : servers) {
						if (server.isEnabled()) {
							addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
						}
					}

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
