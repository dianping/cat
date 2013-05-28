package com.dianping.cat.abtest.repository;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.socket.MessageInboundHandler;
import org.unidal.socket.udp.UdpSocket;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.model.transform.BaseVisitor;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.configuration.ClientConfigManager;

public class DefaultABTestEntityRepository implements ABTestEntityRepository, Initializable {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private InetSocketAddress m_address = new InetSocketAddress("228.0.0.3", 2283);

	private UdpSocket m_socket;

	private String m_domain;

	private Map<Integer, ABTestEntity> m_entities = new HashMap<Integer, ABTestEntity>();

	@Override
	public Map<Integer, ABTestEntity> getAllEntities() {
		return m_entities;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getFirstDomain().getId();

		m_socket = new UdpSocket();
		m_socket.setName("ABTest");
		m_socket.setCodec(new ProtocolMessageCodec());
		m_socket.onMessage(new ProtocolHandler());
		m_socket.listenOn(m_address);

		ProtocolMessage hi = new ProtocolMessage();
		hi.setName(ProtocolNames.HI);
		hi.addHeader(ProtocolNames.HEARTBEAT, m_domain);
		m_socket.send(hi);
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

			if (ProtocolNames.HEARTBEAT.equalsIgnoreCase(name)) {
				String content = message.getContent();
				if (StringUtils.isNotBlank(content)) {
					try {
						AbtestModel abtest = DefaultSaxParser.parse(content);
						ABTestVisitor visitor = new ABTestVisitor();

						abtest.accept(visitor);

						m_entities = visitor.getEntities();
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
	}

	class ABTestVisitor extends BaseVisitor {
		private Map<Integer, ABTestEntity> m_entities;

		public ABTestVisitor() {
			m_entities = new HashMap<Integer, ABTestEntity>();
		}

		@Override
		public void visitCase(Case _case) {
			for (Run run : _case.getRuns()) {
				// filter abtest-entities by domain
				if (run.getDomains() != null && run.getDomains().contains(m_domain)) {
					ABTestEntity abTestEntity = new ABTestEntity(_case, run);

					m_entities.put(abTestEntity.getId(), abTestEntity);
				}
			}
		}

		public Map<Integer, ABTestEntity> getEntities() {
			return m_entities;
		}
	}
}
