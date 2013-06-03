package com.dianping.cat.abtest.repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.model.transform.BaseVisitor;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class HttpABTestEntityRepository extends ContainerHolder implements ABTestEntityRepository, Initializable, Task {

	@Inject
	private ClientConfigManager m_configManager;

	private String m_domain;

	private Map<Integer, ABTestEntity> m_entities = new HashMap<Integer, ABTestEntity>();

	private Map<String, ABTestGroupStrategy> m_strategies = new HashMap<String, ABTestGroupStrategy>();

	@Override
	public Map<Integer, ABTestEntity> getEntities() {
		return m_entities;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getFirstDomain().getId();

	}

	@Override
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();

			try {
				refresh();
			} catch (Throwable e) {
				Cat.logError(e);
			}

			LockSupport.parkUntil(start + 6 * 1000L); // every minute
		}
	}

	@Override
	public void shutdown() {
	}

	private void refresh() {
		String clientIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		for (Server server : m_configManager.getServers()) {
			String ip = server.getIp();
			int port = server.getHttpPort();
			String url = String.format("http://%s:%s/cat/s/abtest?op=model", ip, port);
			Transaction t = Cat.newTransaction("ABTest", url);

			try {
				InputStream inputStream = Urls.forIO().connectTimeout(100).readTimeout(100).openStream(url);
				String content = Files.forIO().readFrom(inputStream, "utf-8");
				AbtestModel abtest = DefaultSaxParser.parse(content);
				ABTestVisitor visitor = new ABTestVisitor(m_domain);

				abtest.accept(visitor);

				// switch the entities
				m_entities = visitor.getEntities();

				Heartbeat h = Cat.newHeartbeat("abtest-heartbeat", clientIp);

				h.addData(abtest.toString());
				h.setStatus(Message.SUCCESS);
				h.complete();

				t.setStatus(Message.SUCCESS);
				break;
			} catch (Throwable e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
	}

	class ABTestVisitor extends BaseVisitor {
		private String m_domain;

		private Map<Integer, ABTestEntity> m_entities;

		public ABTestVisitor(String domain) {
			m_domain = domain;
			m_entities = new HashMap<Integer, ABTestEntity>();
		}

		public Map<Integer, ABTestEntity> getEntities() {
			return m_entities;
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

							ABTestEntity origin = HttpABTestEntityRepository.this.m_entities.get(_case.getId());

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
	}

}
