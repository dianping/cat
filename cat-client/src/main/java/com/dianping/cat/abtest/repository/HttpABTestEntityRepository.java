package com.dianping.cat.abtest.repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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
import com.dianping.cat.configuration.client.entity.Server;

public class HttpABTestEntityRepository extends ContainerHolder implements ABTestEntityRepository, Initializable, Task {

	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private int m_refreshTimeInSeconds = 60; // seconds

	private Map<String, ABTestEntity> m_entities = new HashMap<String, ABTestEntity>();

	private Set<String> m_activeRuns = new HashSet<String>();

	private Map<Integer, ABTestGroupStrategy> m_strategies = new ConcurrentHashMap<Integer, ABTestGroupStrategy>();

	private Map<Integer, Invocable> m_invokeMap = new ConcurrentHashMap<Integer, Invocable>();

	private FieldInjecter m_fieldInjector = new FieldInjecter();

	private ScriptEngine m_engine;

	private String m_domain;

	private long m_lastUpdateTime = -1;

	private String m_abtestModel;

	@Override
	public Map<String, ABTestEntity> getCurrentEntities() {
		return m_entities;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Invocable getInvocable(int runID) {
		return m_invokeMap.get(runID);
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getDomain().getId();
		ScriptEngineManager mgr = new ScriptEngineManager();

		m_engine = mgr.getEngineByExtension("java");
	}

	private void refresh() {
		m_abtestModel = null;

		for (Server server : m_configManager.getServers()) {
			String ip = server.getIp();
			int port = server.getHttpPort();
			String url = String.format("http://%s:%s/cat/s/abtest?op=model&lastUpdateTime=%s", ip, port, m_lastUpdateTime);

			try {
				InputStream inputStream = Urls.forIO().connectTimeout(300).readTimeout(2000).openStream(url);
				String content = Files.forIO().readFrom(inputStream, "utf-8");
				AbtestModel abtest = DefaultSaxParser.parse(content);

				if (abtest.getCases() != null && abtest.getCases().size() > 0) {
					ABTestVisitor visitor = new ABTestVisitor(m_domain);

					abtest.accept(visitor);

					// switch the entities
					m_entities = visitor.getEntities();
					m_activeRuns = visitor.getActiveRuns();
					m_abtestModel = abtest.toString();
					break;
				}
			} catch (Throwable e) {
				Cat.logError(e);
			} finally {
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();
			try {
				// waiting for server start
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				break;
			}

			try {
				refresh();
			} catch (Throwable e) {
				Cat.logError(e);
			}

			LockSupport.parkUntil(start + m_refreshTimeInSeconds * 1000L); // every minute
		}
	}

	public void setRefreshTimeInSeconds(int refreshTimeInSeconds) {
		m_refreshTimeInSeconds = refreshTimeInSeconds;
	}

	@Override
	public void shutdown() {
	}

	public class ABTestVisitor extends BaseVisitor {
		private String m_domain;

		private Map<String, ABTestEntity> m_entities = new HashMap<String, ABTestEntity>();

		private Set<String> m_activeRuns = new HashSet<String>();

		public ABTestVisitor(String domain) {
			m_domain = domain;
		}

		public Map<String, ABTestEntity> getEntities() {
			return m_entities;
		}

		public Set<String> getActiveRuns() {
			return m_activeRuns;
		}

		private void prepareEntity(Case _case, Run run) {
			ABTestEntity entity = new ABTestEntity(_case, run);

			try {
				if (m_strategies.get(run.getId()) != null && m_lastUpdateTime >= run.getLastModifiedDate().getTime()) {
					entity.setGroupStrategy(m_strategies.get(run.getId()));
				} else {
					ABTestGroupStrategy strategy = lookup(ABTestGroupStrategy.class, entity.getGroupStrategyName());

					m_fieldInjector.inject(strategy, run.getGroupstrategyDescriptor());
					strategy.init();
					entity.setGroupStrategy(strategy);
					m_strategies.put(run.getId(), strategy);
				}

				if (m_invokeMap.get(run.getId()) != null && m_lastUpdateTime >= run.getLastModifiedDate().getTime()) {
					entity.setInvocable(m_invokeMap.get(run.getId()));
				} else {
					String javaFragement = run.getConditionsFragement();

					Invocable inv = (Invocable) m_engine.eval(javaFragement);
					entity.setInvocable(inv);
					m_invokeMap.put(run.getId(), inv);
				}

			} catch (Throwable e) {
				Cat.logError(e);
				ABTestEntity origin = m_entities.get(_case.getName());

				if (origin != null) {
					entity = origin;
				} else {
					entity.setDisabled(true);
				}
			} finally {
				m_entities.put(entity.getName(), entity);
			}
		}

		@Override
		public void visitCase(Case _case) {
			long maxUpdateTime = -1;

			for (Run run : _case.getRuns()) {
				m_activeRuns.add(String.valueOf(run.getId()));

				if (run.getDomains() != null && run.getDomains().contains(m_domain)) {
					prepareEntity(_case, run);

					if (run.getLastModifiedDate().getTime() > maxUpdateTime) {
						maxUpdateTime = run.getLastModifiedDate().getTime();
					}
				}
			}

			if (maxUpdateTime > m_lastUpdateTime) {
				m_lastUpdateTime = maxUpdateTime;
			}
		}
	}

	public synchronized String getAbtestModel() {
		return m_abtestModel;
	}

	@Override
	public Set<String> getActiveRuns() {
		return m_activeRuns;
	}
}
