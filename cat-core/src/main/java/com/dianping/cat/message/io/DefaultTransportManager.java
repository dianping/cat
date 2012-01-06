package com.dianping.cat.message.io;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Bind;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.message.spi.MessageManager;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultTransportManager extends ContainerHolder implements TransportManager, Initializable {
	@Inject
	private MessageManager m_manager;

	private MessageSender m_sender;

	private MessageReceiver m_receiver;

	@Override
	public MessageReceiver getReceiver() {
		if (m_receiver == null) {
			throw new RuntimeException("Client mode only, no receiver is provided!");
		}

		return m_receiver;
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
		Config config = m_manager.getConfig();

		if (config == null) {
			// by default, no configuration needed in develop mode, all in memory
			m_sender = lookup(MessageSender.class, "in-memory");
			m_receiver = lookup(MessageReceiver.class, "in-memory");
		} else {
			String mode = config.getMode();

			if ("client".equals(mode)) {
				List<Server> servers = config.getServers();

				if (servers.size() == 1) {
					TcpSocketSender sender = (TcpSocketSender) lookup(MessageSender.class, "tcp-socket");
					Server server = servers.get(0);

					sender.setHost(server.getIp());
					sender.setPort(server.getPort());
					sender.initialize();

					m_sender = sender;
				} else {
					throw new UnsupportedOperationException("Not implemented yet");
				}
			} else if ("server".equals(mode)) {
				TcpSocketReceiver receiver = (TcpSocketReceiver) lookup(MessageReceiver.class, "tcp-socket");
				Bind bind = config.getBind();

				receiver.setHost(bind.getIp());
				receiver.setPort(bind.getPort());
				receiver.initialize();

				m_receiver = receiver;
			} else if ("broker".equals(mode)) {
				throw new UnsupportedOperationException("Not implemented yet");
			} else {
				throw new IllegalArgumentException(String.format("Unsupported mode(%s)!", mode));
			}
		}
	}

	public void setMessageManager(MessageManager manager) {
		m_manager = manager;
	}
}
