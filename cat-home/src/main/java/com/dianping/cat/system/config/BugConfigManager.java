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
import com.dianping.cat.home.bug.config.entity.BugConfig;
import com.dianping.cat.home.bug.config.entity.Domain;
import com.dianping.cat.home.bug.config.transform.DefaultSaxParser;

public class BugConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private BugConfig m_bugConfig;

	private Logger m_logger;

	private static final String CONFIG_NAME = "bugConfig";

	public List<String> queryBugConfigsByDomain(String domain) {
		Domain config = m_bugConfig.findDomain(domain);

		if (config == null) {
			return m_bugConfig.getExceptions();
		} else {
			if (config.getAdditivity() == true) {
				List<String> result = new ArrayList<String>();

				result.addAll(m_bugConfig.getExceptions());
				result.addAll(config.getExceptions());

				return result;
			} else {
				return config.getExceptions();
			}
		}
	}

	public BugConfig getBugConfig() {
		return m_bugConfig;
	}

	public boolean insert(String xml) {
		try {
			m_bugConfig = DefaultSaxParser.parse(xml);
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_bugConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-bug-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_bugConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}