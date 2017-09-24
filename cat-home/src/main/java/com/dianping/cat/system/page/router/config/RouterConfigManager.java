package com.dianping.cat.system.page.router.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.tuple.Pair;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.home.router.transform.DefaultSaxParser;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class RouterConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private DailyReportContentDao m_dailyReportContentDao;

	private int m_configId;

	private volatile RouterConfig m_routerConfig;

	private Logger m_logger;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "routerConfig";

	private Map<Long, Pair<RouterConfig, Long>> m_routerConfigs = new LinkedHashMap<Long, Pair<RouterConfig, Long>>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(
				Entry<Long, Pair<RouterConfig, Long>> eldest) {
			return size() > 100;
		}
	};

	private void addServerList(List<Server> servers, Server server) {
		for (Server s : servers) {
			if (s.getId().equals(server.getId())) {
				return;
			}
		}
		servers.add(server);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public RouterConfig getRouterConfig() {
		return m_routerConfig;
	}

	public Map<Long, Pair<RouterConfig, Long>> getRouterConfigs() {
		return m_routerConfigs;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME,
					ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_routerConfig = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();
				Date now = new Date();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				config.setModifyDate(now);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_routerConfig = DefaultSaxParser.parse(content);
				m_modifyTime = now.getTime();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_routerConfig == null) {
			m_routerConfig = new RouterConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_routerConfig = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();

			return result;
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}

	public Server queryBackUpServer() {
		return new Server().setId(m_routerConfig.getBackupServer()).setPort(
				m_routerConfig.getBackupServerPort());
	}

	public List<Server> queryEnableServers() {
		List<DefaultServer> servers = m_routerConfig.getDefaultServers();
		List<Server> result = new ArrayList<Server>();

		for (DefaultServer server : servers) {
			if (server.isEnable()) {
				result.add(new Server().setId(server.getId())
						.setPort(server.getPort())
						.setWeight(server.getWeight()));
			}
		}
		return result;
	}

	public List<Server> queryServersByDomain(String domain) {
		Domain domainConfig = m_routerConfig.findDomain(domain);
		List<Server> result = new ArrayList<Server>();

		if (domainConfig == null || domainConfig.getServers().isEmpty()) {
			List<Server> servers = queryEnableServers();
			int length = servers.size();
			int hashCode = domain.hashCode();

			if (length > 0) {
				for (int i = 0; i < 2; i++) {
					int index = Math.abs((hashCode + i)) % length;

					addServerList(result, servers.get(index));
				}
			}

			addServerList(result, queryBackUpServer());
		} else {
			for (Server server : domainConfig.getServers()) {
				result.add(server);
			}
		}
		return result;
	}

	public void refreshConfig() throws Exception {
		refreshConfigInfo();
		refreshReportInfo();
	}

	private void refreshReportInfo() throws Exception {
		Date period = TimeHelper.getCurrentDay(-1);
		long time = period.getTime();

		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(
					Constants.CAT, RouterConfigBuilder.ID, period,
					DailyReportEntity.READSET_FULL);
			long modifyTime = report.getCreationDate().getTime();
			Pair<RouterConfig, Long> pair = m_routerConfigs.get(time);

			if (pair == null || modifyTime > pair.getValue()) {
				try {
					DailyReportContent reportContent = m_dailyReportContentDao
							.findByPK(report.getId(),
									DailyReportContentEntity.READSET_FULL);
					RouterConfig routerConfig = DefaultNativeParser
							.parse(reportContent.getContent());

					m_routerConfigs.put(time, new Pair<RouterConfig, Long>(
							routerConfig, modifyTime));
					Cat.logEvent("ReloadConfig", "router");
				} catch (DalNotFoundException e) {
					// ignore
				} catch (Exception e) {
					throw e;
				}
			}
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			throw e;
		}
	}

	private void refreshConfigInfo() throws DalException, SAXException,
			IOException {
		Config config = m_configDao.findByName(CONFIG_NAME,
				ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				RouterConfig routerConfig = DefaultSaxParser.parse(content);

				m_routerConfig = routerConfig;
				m_modifyTime = modifyTime;
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_routerConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}