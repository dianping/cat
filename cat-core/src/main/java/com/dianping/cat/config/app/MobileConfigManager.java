package com.dianping.cat.config.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.mobile.entity.ConstantItem;
import com.dianping.cat.configuration.mobile.entity.Item;
import com.dianping.cat.configuration.mobile.entity.MobileConfig;
import com.dianping.cat.configuration.mobile.entity.Type;
import com.dianping.cat.configuration.mobile.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class MobileConfigManager implements Initializable, LogEnabled {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private Logger m_logger;

	private int m_configId;

	private long m_modifyTime;

	private volatile MobileConfig m_config;

	private static final String CONFIG_NAME = "mobile-config";

	private volatile Map<String, Integer> m_cities = new ConcurrentHashMap<String, Integer>();

	private volatile Map<String, Integer> m_cities2 = new ConcurrentHashMap<String, Integer>();

	private volatile Map<String, Integer> m_operators = new ConcurrentHashMap<String, Integer>();

	private volatile Map<String, Integer> m_operators2 = new ConcurrentHashMap<String, Integer>();

	private volatile Map<String, Integer> m_platforms = new ConcurrentHashMap<String, Integer>();

	public boolean addConstant(String type, int id, String value) {
		ConstantItem item = m_config.findConstantItem(type);

		if (item != null) {
			Item data = item.getItems().get(id);

			if (data != null) {
				data.setValue(value);
			} else {
				data = new Item(id);
				data.setValue(value);

				item.getItems().put(id, data);
			}
			return storeConfig();
		} else {
			return false;
		}
	}

	private Map<String, Integer> buildConstantCache(String key) {
		ConstantItem items = m_config.findConstantItem(key);
		Map<String, Integer> results = new ConcurrentHashMap<String, Integer>();

		if (items != null && items.getItems() != null) {
			for (Item item : items.getItems().values()) {
				String value = item.getValue();
				String[] values = value.split(MobileConstants.SPLITTER);

				for (String v : values) {
					results.put(v, item.getId());
				}
			}
		}
		return results;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getBrokerName() {
		return getConfigByKey(MobileConstants.BROKER_KEY, "broker-service");
	}

	public MobileConfig getConfig() {
		return m_config;
	}

	public String getConfigByKey(String key, String defaultValue) {
		com.dianping.cat.configuration.mobile.entity.Config item = m_config.findConfig(key);

		if (item != null) {
			return item.getValue();
		} else {
			return defaultValue;
		}
	}

	public ConstantItem getConstantItemByCategory(String category) {
		ConstantItem item = m_config.findConstantItem(category);

		if (item != null) {
			return item;
		}
		return null;
	}

	public String getConstantItemValue(String category, int id, String defaultValue) {
		ConstantItem item = m_config.findConstantItem(category);

		if (item != null) {
			Item i = item.findItem(id);

			if (i != null) {
				return i.getValue();
			}
		}
		return defaultValue;
	}

	public List<String> getInvalidatePatterns() {
		String patterns = getConfigByKey(MobileConstants.INVALID_PATTERN, "");

		return Splitters.by(",").noEmptyItem().split(patterns);
	}

	public String getLocalFlushPath() {
		return m_config.getCheckpoint().getLocalFlush().getPath();
	}

	public String getLogPath(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.getPath();
		} else {
			return null;
		}
	}

	public int getLogReservceDuration(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.getDurationInHours();
		} else {
			return 3;
		}
	}

	public String getNamespace(int sourceId) {
		Item item = m_config.findConstantItem(MobileConstants.SOURCE).findItem(sourceId);

		if (item != null) {
			return item.getValue();
		} else {
			return AppCommandConfigManager.DEFAULT_NAMESPACE;
		}
	}

	public String getAppName(int appId) {
		Map<Integer, Item> sources = queryConstantItem(MobileConstants.SOURCE);
		Item item = sources.get(appId);
		String appName = null;

		if (item != null) {
			appName = item.getValue();
		} else {
			appName = String.valueOf(appId);
		}

		return appName;
	}

	public int getPlatformId(String platform) {
		return m_platforms.get(platform);
	}

	public String getPlatformStr(int platform) {
		ConstantItem configItem = m_config.findConstantItem(MobileConstants.PLATFORM);
		Item item = configItem.findItem(platform);

		return item.getValue();
	}

	public String getTooLongCommand() {
		return getConfigByKey(MobileConstants.TOO_LONG_COMMAND_KEY, "toolongurl.bin");
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
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
				m_logger.error(ex.getMessage());
			}
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage());
		}
		if (m_config == null) {
			m_config = new MobileConfig();
		}
		
		refreshData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}
		});
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

	public boolean isDevMode() {
		return Boolean.parseBoolean(getConfigByKey(MobileConstants.DEV_MODE_KEY, "false"));
	}

	public boolean isGroupStatisticEnabled() {
		return Boolean.parseBoolean(getConfigByKey(MobileConstants.GROUP_STAT_KEY, "true"));
	}

	public boolean isLocalFlushEnabled() {
		return m_config.getCheckpoint().isLocalFlushEnabled();
	}

	public boolean isRemteIpServiceEnabled() {
		return Boolean.parseBoolean(getConfigByKey(MobileConstants.REMOTE_IP_KEY, "true"));
	}

	public Map<Integer, Item> queryConstantItem(String name) {
		ConstantItem config = m_config.findConstantItem(name);

		if (config != null) {
			return config.getItems();
		} else {
			return new ConcurrentHashMap<Integer, Item>();
		}
	}

	public Item queryConstantItem(String category, int id) {
		ConstantItem item = m_config.findConstantItem(category);

		if (item != null) {
			return item.findItem(id);
		}
		return null;
	}

	public Collection<Item> queryApps() {
		return queryConstantItem(MobileConstants.SOURCE).values();
	}

	public int queryNamespaceIdByTitle(String namespace) {
		ConstantItem item = m_config.findConstantItem(MobileConstants.SOURCE);

		for (Item i : item.getItems().values()) {
			if (namespace.equalsIgnoreCase(i.getValue())) {
				return i.getId();
			}
		}
		return -1;
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				MobileConfig MobileConfig = DefaultSaxParser.parse(content);
				m_config = MobileConfig;
				m_modifyTime = modifyTime;

				refreshData();
			}
		}
	}

	public Integer getCityId(String city) {
		Integer cityId = m_cities.get(city);

		if (cityId == null) {
			cityId = m_cities2.get(city);
		}

		if (cityId == null) {
			cityId = m_cities.get(MobileConstants.OTHER);
		}

		return cityId;
	}

	public Integer getOperatorId(String operator) {
		Integer operatorId = m_operators.get(operator);

		if (operatorId == null) {
			operatorId = m_operators2.get(operator);
		}

		if (operatorId == null) {
			operatorId = m_operators.get(MobileConstants.OTHER);
		}

		return operatorId;
	}

	private void refreshData() {
		m_cities = buildConstantCache(MobileConstants.CITY);
		m_operators = buildConstantCache(MobileConstants.OPERATOR);
		m_platforms = buildConstantCache(MobileConstants.PLATFORM);
	}

	public void setConfigDao(ConfigDao dao) {
		m_configDao = dao;
	}

	public boolean shouldAutoPrune() {
		return Boolean.parseBoolean(getConfigByKey(MobileConstants.AUTO_PRUNE_KEY, "false"));
	}

	public boolean shouldLog(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.isEnabled();
		} else {
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
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);

				refreshData();
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
