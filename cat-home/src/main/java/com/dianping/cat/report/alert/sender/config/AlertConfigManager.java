package com.dianping.cat.report.alert.sender.config;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.alert.config.entity.AlertConfig;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.home.alert.config.transform.DefaultSaxParser;

public class AlertConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private AlertConfig m_config;

	private static final String CONFIG_NAME = "alertConfig";

	public String buildReceiverContentByOnOff(String originXml, String allOnOrOff) {
		try {
			if (StringUtils.isEmpty(allOnOrOff)) {
				return originXml;
			}

			AlertConfig tmpConfig = DefaultSaxParser.parse(originXml);

			if (allOnOrOff.equals("on")) {
				turnOnOrOffConfig(tmpConfig, true);
			} else if (allOnOrOff.equals("off")) {
				turnOnOrOffConfig(tmpConfig, false);
			}

			return tmpConfig.toString();
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	public AlertConfig getAlertConfig() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new AlertConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public Receiver queryReceiverById(String id) {
		return m_config.getReceivers().get(id);
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	private void turnOnOrOffConfig(AlertConfig config, boolean isOn) {
		for (Receiver receiver : config.getReceivers().values()) {
			receiver.setEnable(isOn);
		}
	}

}
