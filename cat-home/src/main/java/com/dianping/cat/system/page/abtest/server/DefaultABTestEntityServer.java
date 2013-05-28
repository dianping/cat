package com.dianping.cat.system.page.abtest.server;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.socket.MessageInboundHandler;
import org.unidal.socket.udp.UdpSocket;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.model.transform.DefaultXmlBuilder;
import com.dianping.cat.abtest.repository.ProtocolMessage;
import com.dianping.cat.abtest.repository.ProtocolMessageCodec;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyEntity;
import com.dianping.cat.system.page.abtest.AbtestStatus;

/**
 * send heartbeat(传输ABTestModel) every interval time.<br>
 * receive hello from client, and reply heartbeat.<br>
 * 一个case如果有多个run，只发当前最接近的一个run(不能多个run，cleint以caseId为key存储run的，所以client也只需一个run即可) server发送的run，是当前+最近即将生效的run（构建成ABTestModel来传输）。
 * 
 * @author wukezhu
 */
public class DefaultABTestEntityServer implements ABTestEntityServer, Initializable, LogEnabled {

	private static final String MULTICAST_ADDRESS = "228.0.0.3";

	private static final int MULTICAST_PORT = 2283;

	private static final int MINUTE_PERIOD = 60 * 1000;

	private static final int HALF_MINUTE_PERIOD = 30 * 1000;

	private Logger m_logger;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private AbtestDao abtestDao;

	@Inject
	private GroupStrategyDao groupStrategyDao;

	@Inject
	private InetSocketAddress m_address = new InetSocketAddress(MULTICAST_ADDRESS, MULTICAST_PORT);

	private UdpSocket m_socket;

	private Timer m_timer = new Timer();

	private AbtestModel m_abtestModel;

	@Override
	public void initialize() throws InitializationException {

		m_socket = new UdpSocket();
		m_socket.setName("ABTest");
		m_socket.setCodec(new ProtocolMessageCodec());
		m_socket.onMessage(new ProtocolHandler());
		m_socket.listenOn(m_address);

		// 启动RefreshEntityTask任务
		RefreshEntityTask refreshEntityTask = new RefreshEntityTask();
		m_timer.schedule(refreshEntityTask, 0, HALF_MINUTE_PERIOD);

		// 启动heartbeatTask任务
		HeartbeatTask heartbeatTask = new HeartbeatTask();
		m_timer.schedule(heartbeatTask, 0, MINUTE_PERIOD);
	}

	public void setAddress(String address) {
		List<String> parts = Splitters.by(':').trim().split(address);
		int len = parts.size();
		int index = 0;
		String host = len > index ? parts.get(index++) : MULTICAST_ADDRESS;
		int port = len > index ? Integer.parseInt(parts.get(index++)) : MULTICAST_PORT;

		m_address = new InetSocketAddress(host, port);
	}

	class RefreshEntityTask extends TimerTask {

		@Override
		public void run() {
			try {
				AbtestModel abtestModel = new AbtestModel();

				List<AbtestRun> abtestRuns = m_abtestRunDao.findAll(AbtestRunEntity.READSET_FULL);

				if (abtestRuns != null) {
					Date now = new Date();
					for (AbtestRun abtestRun : abtestRuns) {
						AbtestStatus status = AbtestStatus.calculateStatus(abtestRun, now);
						if (status == AbtestStatus.READY || status == AbtestStatus.RUNNING) {
							// fetch Case and GroupStrategy
							int caseId = abtestRun.getCaseId();
							Abtest entity = abtestDao.findByPK(caseId, AbtestEntity.READSET_FULL);
							int gid = entity.getGroupStrategy();
							GroupStrategy groupStrategy = groupStrategyDao.findByPK(gid, GroupStrategyEntity.READSET_FULL);

							Case _case = transform(abtestRun, entity, groupStrategy);
							abtestModel.addCase(_case);

						}
					}
				}

				m_abtestModel = abtestModel;

			} catch (DalException e) {
				m_logger.error("Error when find all AbtestRun", e);
				Cat.logError(e);
			}
		}
	}

	private Case transform(AbtestRun abtestRun, Abtest entity, GroupStrategy groupStrategy) throws DalException {
		Case _case = new Case(entity.getId());
		_case.setCreatedDate(entity.getCreationDate());
		_case.setDescription(entity.getDescription());
		_case.setGroupStrategy(groupStrategy.getName());
		_case.setName(entity.getName());
		_case.setOwner(entity.getOwner());
		_case.setLastModifiedDate(entity.getModifiedDate());
		for (String domain : StringUtils.split(entity.getDomains(), ',')) {
			_case.addDomain(domain);
		}

		Run run = new Run(abtestRun.getId());
		run.setCreator(abtestRun.getCreator());
		run.setDisabled(false);
		run.setEndDate(abtestRun.getEndDate());
		run.setGroupStrategyConfiguration(abtestRun.getStrategyConfiguration());
		run.setStartDate(abtestRun.getStartDate());

		_case.addRun(run);

		return _case;
	}

	class HeartbeatTask extends TimerTask {
		@Override
		public void run() {
			sendHeartbeatMessage();
		}
	}

	class ProtocolHandler implements MessageInboundHandler<ProtocolMessage> {
		@Override
		public void handle(ProtocolMessage message) {
			String name = message.getName();

			if (ProtocolMessage.HELLO_NAME.equals(name)) {
				sendHeartbeatMessage();
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void sendHeartbeatMessage() {
		ProtocolMessage message = new ProtocolMessage();
		message.setName(ProtocolMessage.HEARTBEAT_NAME);
		String content = new DefaultXmlBuilder().buildXml(m_abtestModel);
		message.setContent(content);

		m_socket.send(message);
	}
}
