package com.dianping.cat.system.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.utilization.config.entity.UtilizationConfig;
import com.dianping.cat.home.utilization.config.transform.DefaultSaxParser;

public class UtilizationConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private UtilizationConfig m_utilizationConfig;

	private static final String CONFIG_NAME = "utilizationConfig";

	public UtilizationConfig getUtilizationConfig() {
		return m_utilizationConfig;
	}

	public boolean insert(String xml) {
		try {
			m_utilizationConfig = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();
			return result;
		} catch (Exception e) {
			Cat.logError(e);
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
				config.setContent(m_utilizationConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_utilizationConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-utilization-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_utilizationConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_utilizationConfig == null) {
			m_utilizationConfig = new UtilizationConfig();
		}
	}

}
