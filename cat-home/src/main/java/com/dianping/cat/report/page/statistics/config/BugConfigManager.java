package com.dianping.cat.report.page.statistics.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.bug.config.entity.BugConfig;
import com.dianping.cat.home.bug.config.entity.Domain;
import com.dianping.cat.home.bug.config.transform.DefaultSaxParser;

public class BugConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private BugConfig m_bugConfig;

	private Logger m_logger;

	private static final String CONFIG_NAME = "bugConfig";

	private Map<String, Set<String>> m_configs = new HashMap<String, Set<String>>();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public BugConfig getBugConfig() {
		return m_bugConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_bugConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_bugConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_bugConfig == null) {
			m_bugConfig = new BugConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_bugConfig = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();
			m_configs.clear();
			return result;
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}

	public Set<String> queryBugConfigsByDomain(String domain) {
		Set<String> result = m_configs.get(domain);

		if (result == null) {
			result = new HashSet<String>();
			Domain config = m_bugConfig.findDomain(domain);

			if (config == null) {
				result = new HashSet<String>(m_bugConfig.getExceptions());
			} else {
				if (config.getAdditivity() == true) {
					result.addAll(m_bugConfig.getExceptions());
					result.addAll(config.getExceptions());
				} else {
					result = new HashSet<String>(config.getExceptions());
				}
			}
			m_configs.put(domain, result);
		}
		return result;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_bugConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}