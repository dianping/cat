package com.dianping.cat.consumer.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.util.StringUtils;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricAnalyzer.ConfigItem;
import com.dianping.cat.consumer.metric.config.entity.MetricConfig;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.config.entity.Tag;
import com.dianping.cat.consumer.metric.config.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class MetricConfigManager implements Initializable, LogEnabled {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	private int m_configId;

	private volatile MetricConfig m_metricConfig;

	private long m_modifyTime;

	private Logger m_logger;

	private static final String CONFIG_NAME = "metricConfig";

	public static final String DEFAULT_TAG = "业务大盘";

	public String buildMetricKey(String domain, String type, String metricKey) {
		return domain + ":" + type + ":" + metricKey;
	}

	public boolean deleteBatchDomainConfig(String domain) {
		Set<String> keys = new HashSet<String>();

		Map<String, MetricItemConfig> metricItemConfigs = getMetricConfig().getMetricItemConfigs();
		
		for (Entry<String, MetricItemConfig> entry : metricItemConfigs.entrySet()) {
			String currentKey = entry.getKey();

			if (currentKey.startsWith(domain + ":Metric:")) {
				keys.add(currentKey);
			}
		}

		for (String key : keys) {
			getMetricConfig().removeMetricItemConfig(key);
		}
		return storeConfig();
	}

	public boolean deleteDomainConfig(String key) {
		getMetricConfig().removeMetricItemConfig(key);
		return storeConfig();
	}

	protected void deleteUnusedConfig() {
		try {
			Map<String, MetricItemConfig> configs = m_metricConfig.getMetricItemConfigs();
			List<String> unused = new ArrayList<String>();

			for (MetricItemConfig config : configs.values()) {
				String domain = config.getDomain();
				String productLine = m_productLineConfigManager.queryProductLineByDomain(domain);
				ProductLineConfig productLineConfig = m_productLineConfigManager.queryProductLine(productLine);

				if (ProductLineConfig.METRIC.equals(productLineConfig)) {
					unused.add(config.getId());
				}
			}
			for (String id : unused) {
				m_logger.info("delete metric item " + id);
				m_metricConfig.removeMetricItemConfig(id);
			}
			storeConfig();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public MetricConfig getMetricConfig() {
		synchronized (this) {
			return m_metricConfig;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			m_configId = config.getId();
			m_metricConfig = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_metricConfig = DefaultSaxParser.parse(content);
				m_modifyTime = new Date().getTime();
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

	public boolean insertMetricIfNotExist(String domain, String type, String metricKey, ConfigItem item) {
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
			m_logger.info("insert metric config info " + config.toString());
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

	public List<MetricItemConfig> queryMetricItemConfigs(Collection<String> domains) {
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

	public List<MetricItemConfig> queryMetricItemConfigs(String tag) {
		List<MetricItemConfig> metricItemConfigs = new ArrayList<MetricItemConfig>();

		try {
			Collection<MetricItemConfig> configs = getMetricConfig().getMetricItemConfigs().values();

			for (MetricItemConfig metricItemConfig : configs) {
				for (Tag itemTag : metricItemConfig.getTags()) {
					String tagName = itemTag.getName();

					if (tag.equals(tagName)) {
						metricItemConfigs.add(metricItemConfig);
						break;
					}
				}
			}
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return metricItemConfigs;
	}

	public List<String> queryTags() {
		Set<String> tags = new HashSet<String>();
		try {
			Collection<MetricItemConfig> configs = getMetricConfig().getMetricItemConfigs().values();

			for (MetricItemConfig metricItemConfig : configs) {
				for (Tag tag : metricItemConfig.getTags()) {
					String tagName = tag.getName();
					if (!StringUtils.isEmpty(tagName)) {
						tags.add(tagName);
					}
				}
			}
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		List<String> result = new ArrayList<String>(tags);
		Collections.sort(result, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.equals(DEFAULT_TAG)) {
					return -1;
				}
				if (o2.endsWith(DEFAULT_TAG)) {
					return 1;
				}
				return o1.compareTo(o2);
			}
		});

		return result;
	}

	public void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				MetricConfig metricConfig = DefaultSaxParser.parse(content);

				m_metricConfig = metricConfig;
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
				config.setContent(getMetricConfig().toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
