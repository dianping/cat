package com.dianping.cat.config.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.comparison.entity.AppComparison;
import com.dianping.cat.configuration.app.comparison.entity.AppComparisonConfig;
import com.dianping.cat.configuration.app.comparison.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class AppComparisonConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private volatile AppComparisonConfig m_config;

	private static final String CONFIG_NAME = "appComparisonConfig";

	private int m_configId;

	public AppComparisonConfig getConfig() {
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

	public Map<String, AppComparison> queryAppComparisons() {
		return m_config.getAppComparisons();
	}

	public List<String> queryEmails(String id) {
		List<String> emails = new ArrayList<String>();
		AppComparison appComparison = m_config.getAppComparisons().get(id);

		if (appComparison != null) {
			emails.addAll(Splitters.by(",").noEmptyItem().split(appComparison.getEmails()));
		}
		return emails;
	}

	public String queryEmailStr(String id) {
		AppComparison appComparison = m_config.getAppComparisons().get(id);

		if (appComparison != null) {
			return appComparison.getEmails();
		}
		return null;
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

}
