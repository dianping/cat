package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
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

	private int m_configId;

	private RouterConfig m_routerConfig;

	private Logger m_logger;

	private static final String CONFIG_NAME = "routerConfig";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public RouterConfig getRouterConfig() {
		return m_routerConfig;
	}

	public Server queryBackUpServer() {
		return new Server().setId(m_routerConfig.getBackupServer()).setPort(m_routerConfig.getBackupServerPort());
	}

	private void addServerList(List<Server> servers, Server server) {
		for (Server s : servers) {
			if (s.getId().equals(server.getId())) {
				return;
			}
		}
		servers.add(server);
	}

	public List<Server> queryServersByDomain(String domain) {
		Domain domainConfig = m_routerConfig.findDomain(domain);
		List<Server> result = new ArrayList<Server>();

		if (domainConfig == null) {
			List<Server> servers = queryEnableServers();
			int length = servers.size();
			int index = domain.hashCode();

			for (int i = 0; i < 2; i++) {
				addServerList(result, servers.get((index + i) % length));
			}
			addServerList(result, queryBackUpServer());
		} else {
			for (Server server : domainConfig.getServers()) {
				result.add(server);
			}
		}
		return result;
	}

	public List<Server> queryEnableServers() {
		List<DefaultServer> servers = m_routerConfig.getDefaultServers();
		List<Server> result = new ArrayList<Server>();

		for (DefaultServer server : servers) {
			if (server.isEnable()) {
				result.add(new Server().setId(server.getId()).setPort(server.getPort()));
			}
		}
		return result;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_routerConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-router-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_routerConfig = DefaultSaxParser.parse(content);
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