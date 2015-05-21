package com.dianping.cat.system.page.router.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.transform.DefaultSaxParser;

public class RouterConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private volatile RouterConfig m_routerConfig;

	private Logger m_logger;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "routerConfig";

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

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
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
		return new Server().setId(m_routerConfig.getBackupServer()).setPort(m_routerConfig.getBackupServerPort());
	}

	public List<Server> queryEnableServers() {
		List<DefaultServer> servers = m_routerConfig.getDefaultServers();
		List<Server> result = new ArrayList<Server>();

		for (DefaultServer server : servers) {
			if (server.isEnable()) {
				result.add(new Server().setId(server.getId()).setPort(server.getPort()).setWeight(server.getWeight()));
			}
		}
		return result;
	}

	public List<Server> queryServersByDomain(String domain) {
		Domain domainConfig = m_routerConfig.findDomain(domain);
		List<Server> result = new ArrayList<Server>();

		if (domainConfig == null) {
			List<Server> servers = queryEnableServers();
			int length = servers.size();
			int hashCode = domain.hashCode();

			for (int i = 0; i < 2; i++) {
				int index = Math.abs((hashCode + i)) % length;

				addServerList(result, servers.get(index));
			}
			addServerList(result, queryBackUpServer());
		} else {
			for (Server server : domainConfig.getServers()) {
				result.add(server);
			}
		}
		return result;
	}

	public void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
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