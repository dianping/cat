package com.dianping.cat.abtest.repository;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.socket.MessageInboundHandler;
import org.unidal.socket.udp.UdpSocket;

import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.configuration.ClientConfigManager;

public class DefaultABTestEntityRepository implements ABTestEntityRepository, Initializable {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private InetSocketAddress m_address = new InetSocketAddress("228.0.0.3", 2283);

	private UdpSocket m_socket;

	private String m_domain;

	private List<ABTestEntity> m_entities = new ArrayList<ABTestEntity>();

	@Override
	public List<ABTestEntity> getEntities(Date from, Date to) {
		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getFirstDomain().getId();

		m_socket = new UdpSocket();
		m_socket.setName("ABTest");
		m_socket.setCodec(new ProtocolMessageCodec());
		m_socket.onMessage(new ProtocolHandler());
		m_socket.listenOn(m_address);
	}

	public void setAddress(String address) {
		List<String> parts = Splitters.by(':').trim().split(address);
		int len = parts.size();
		int index = 0;
		String host = len > index ? parts.get(index++) : "228.0.0.3";
		String port = len > index ? parts.get(index++) : "2283";

		m_address = new InetSocketAddress(host, Integer.parseInt(port));
	}

	class ProtocolHandler implements MessageInboundHandler<ProtocolMessage> {
		@Override
		public void handle(ProtocolMessage message) {
			String name = message.getName();

			if ("hi".equals(name)) {
			} else if ("heartbeat".equals(name)) {
				List<ABTestEntity> entities = new ArrayList<ABTestEntity>();

				m_entities = entities;
			}
		}
	}
}
