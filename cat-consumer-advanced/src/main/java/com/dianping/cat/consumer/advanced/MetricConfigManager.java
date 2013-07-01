package com.dianping.cat.consumer.advanced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricConfig;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.advanced.metric.config.transform.DefaultSaxParser;
import com.dianping.cat.consumer.advanced.MetricAnalyzer.ConfigItem;
import com.dianping.cat.consumer.core.config.Config;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.config.ConfigEntity;

public class MetricConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private MetricConfig m_metricConfig;

	private long m_modifyTime;

	private Logger m_logger;

	private static final String CONFIG_NAME = "metricConfig";

	public String buildMetricKey(String domain, String type, String metricKey) {
		return domain + ":" + type + ":" + metricKey;
	}

	public boolean deleteDomainConfig(String key) {
		getMetricConfig().removeMetricItemConfig(key);
		return storeConfig();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public MetricConfig getMetricConfig() {
		synchronized (m_metricConfig) {
			return m_metricConfig;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_metricConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-metric-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_metricConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
				m_modifyTime = config.getModifyDate().getTime();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_metricConfig == null) {
			m_metricConfig = new MetricConfig();
		}
	}

	public boolean insertIfNotExist(String domain, String type, String metricKey, ConfigItem item) {
		String key = buildMetricKey(domain, type, metricKey);
		MetricItemConfig config = m_metricConfig.findMetricItemConfig(key);

		if (config != null) {
			return true;
		} else {
			config = new MetricItemConfig();
			
			config.setId(key);
			config.setDomain(domain);
			config.setType(type);
			config.setMetricKey(metricKey);
			config.setTitle(item.getTitle());
			config.setShowAvg(item.isShowAvg());
			config.setShowCount(item.isShowCount());
			config.setShowSum(item.isShowSum());
			return insertMetricItemConfig(config);
		}
	}

	public boolean insertMetricItemConfig(MetricItemConfig config) {
		getMetricConfig().addMetricItemConfig(config);

		return storeConfig();
	}

	public MetricItemConfig queryMetricItemConfig(String id) {
		return getMetricConfig().findMetricItemConfig(id);
	}

	public List<MetricItemConfig> queryMetricItemConfigs(Set<String> domains) {
		List<MetricItemConfig> configs = new ArrayList<MetricItemConfig>();
		Map<String, MetricItemConfig> metricConfig = getMetricConfig().getMetricItemConfigs();

		for (Entry<String, MetricItemConfig> entry : metricConfig.entrySet()) {
			MetricItemConfig item = entry.getValue();

			if (domains.contains(item.getDomain())) {
				configs.add(item);
			}
		}
		Collections.sort(configs, new Comparator<MetricItemConfig>() {

			@Override
			public int compare(MetricItemConfig m1, MetricItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});
		return configs;
	}

	public void refreshMetricConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		if (modifyTime > m_modifyTime) {
			String content = config.getContent();

			synchronized (m_metricConfig) {
				m_metricConfig = DefaultSaxParser.parse(content);
			}

			m_modifyTime = modifyTime;
			m_logger.info("metric config refresh done!");
		}
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(getMetricConfig().toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

}
