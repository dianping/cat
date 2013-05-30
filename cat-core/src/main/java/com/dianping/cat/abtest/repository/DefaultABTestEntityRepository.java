package com.dianping.cat.abtest.repository;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.lookup.ContainerHolder;
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
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;

public class DefaultABTestEntityRepository extends ContainerHolder implements ABTestEntityRepository, Initializable,
      LogEnabled, MessageInboundHandler<ProtocolMessage> {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private InetSocketAddress m_address = new InetSocketAddress("228.0.0.3", 2283);

	@Inject
	private Logger m_logger;

	private UdpSocket m_socket;

	private String m_domain;

	private Map<Integer, ABTestEntity> m_entities = new HashMap<Integer, ABTestEntity>();

	private Map<String, ABTestGroupStrategy> m_strategies = new HashMap<String, ABTestGroupStrategy>();

	@Override
	public Map<Integer, ABTestEntity> getEntities() {
		return m_entities;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getFirstDomain().getId();
	}

	// for test purpose
	void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public void start() {
		m_socket = new UdpSocket();
		m_socket.setName("ABTest");
		m_socket.setCodec(new ProtocolMessageCodec());
		m_socket.onMessage(this);
		m_socket.listenOn(m_address);

		System.out.println("ABTestRepository init...");

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

	@Override
	public void handle(ProtocolMessage message) {
		String name = message.getName();

		if (ProtocolNames.HEARTBEAT.equalsIgnoreCase(name)) {
			Heartbeat h = Cat.newHeartbeat("abtest-heartbeat", message.getName()); // TODO .getFrom()

			h.addData(message.toString());
			h.setStatus(Message.SUCCESS);
			h.complete();

			String content = message.getContent();

			if (StringUtils.isNotBlank(content)) {
				try {
					AbtestModel abtest = DefaultSaxParser.parse(content);
					ABTestVisitor visitor = new ABTestVisitor(m_domain);

					abtest.accept(visitor);
					
					//switch the entities
					m_entities = visitor.getEntities();
					
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} else {
			m_logger.warn(String.format("Unknown command(%s) found in %s!", name, message));
		}
	}

	class ABTestVisitor extends BaseVisitor {
		private String m_domain;

		private Map<Integer, ABTestEntity> m_entities;

		public ABTestVisitor(String domain) {
			m_domain = domain;
			m_entities = new HashMap<Integer, ABTestEntity>();
		}

		@Override
		public void visitCase(Case _case) {
			for (Run run : _case.getRuns()) {
				// filter abtest-entities by domain
				if (run.getDomains() != null && run.getDomains().contains(m_domain)) {

					ABTestEntity entity = new ABTestEntity(_case, run);
					
					String strategyKey = String.format("%s+%s+%s", _case.getId(), entity.getGroupStrategyName(),
					      entity.getGroupStrategyConfiguration());
					ABTestGroupStrategy strategy = m_strategies.get(strategyKey);

					if (strategy != null) {
						entity.setGroupStrategy(strategy);
					} else {
						try {
							strategy = lookup(ABTestGroupStrategy.class, entity.getGroupStrategyName());
							entity.setGroupStrategy(strategy);

							m_strategies.put(strategyKey, strategy);
						} catch (Exception e) {
							Cat.logError(e);
							
							ABTestEntity origin = DefaultABTestEntityRepository.this.m_entities.get(_case.getId());
							
							if (origin != null) {
								entity = origin;
							} else {
								entity.setDisabled(true);
							}
						}
					}

					m_entities.put(entity.getId(), entity);
				}
			}
		}

		
		public Map<Integer, ABTestEntity> getEntities() {
      	return m_entities;
      }
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
