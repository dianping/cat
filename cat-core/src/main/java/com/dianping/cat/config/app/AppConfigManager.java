package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.app.entity.AppConfig;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.ConfigItem;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class AppConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	private Map<String, Integer> m_commands = new HashMap<String, Integer>();

	private Map<String, Integer> m_cities = new HashMap<String, Integer>();

	private Map<String, Integer> m_operators = new HashMap<String, Integer>();

	private int m_configId;

	private static final String CONFIG_NAME = "app-config";

	private AppConfig m_config;

	private long m_modifyTime;

	public static String NETWORK = "网络类型";

	public static String OPERATOR = "运营商";

	public static String VERSION = "版本";

	public static String PLATFORM = "平台";

	public static String CITY = "城市";

	public static String CONNECT_TYPE = "连接类型";

	public static final String ACTIVITY_PREFIX = "http://tgapp.dianping.com/activity/";

	public boolean addCommand(String domain, String title, String name) throws Exception {
		Command command = new Command();

		command.setDomain(domain);
		command.setTitle(title);
		command.setName(name);

		boolean isActivityCommand = name.startsWith(ACTIVITY_PREFIX);
		if (isActivityCommand) {
			command.setId(findAvailableId(1000, 1500));
		} else {
			command.setId(findAvailableId(1, 200));
		}

		m_config.addCommand(command);
		return storeConfig();
	}

	public boolean deleteCommand(String domain, String name) {
		Set<Entry<Integer, Command>> entries = m_config.getCommands().entrySet();
		List<Integer> needDeleteIds = new ArrayList<Integer>();

		for (Entry<Integer, Command> entry : entries) {
			Command command = entry.getValue();
			if (domain.equals(command.getName()) && name.equals(command.getName())) {
				needDeleteIds.add(command.getId());
			}
		}
		for (int id : needDeleteIds) {
			m_config.removeCommand(id);
		}
		return storeConfig();
	}

	private int findAvailableId(int startIndex, int endIndex) throws Exception {
		Set<Integer> keys = m_config.getCommands().keySet();
		int maxKey = 0;

		for (int key : keys) {
			if (key >= startIndex && key <= endIndex && key > maxKey) {
				maxKey = key;
			}
		}
		if (maxKey < endIndex) {
			return maxKey + 1;
		} else {
			for (int i = startIndex; i <= endIndex; i++) {
				if (!keys.contains(i)) {
					return i;
				}
			}

			Exception ex = new RuntimeException();
			Cat.logError("app config range is full: " + startIndex + " - " + endIndex, ex);
			throw ex;
		}
	}

	public Map<String, Integer> getCities() {
		return m_cities;
	}

	public Map<String, Integer> getCommands() {
		return m_commands;
	}

	public AppConfig getConfig() {
		return m_config;
	}

	public Map<String, Integer> getOperators() {
		return m_operators;
	}

	public Map<Integer, Command> getRawCommands() {
		return m_config.getCommands();
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
			refreshData();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-app-config.xml"), "utf-8");
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
			m_config = new AppConfig();
		}
		Threads.forGroup("cat").start(new ConfigReloadTask());
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

	public Map<Integer, Code> queryCodeByCommand(int command) {
		Command c = m_config.findCommand(command);

		if (c != null) {
			Map<Integer, Code> values = c.getCodes();
			Map<Integer, Code> result = new HashMap<Integer, Code>(values);

			result.putAll(m_config.getCodes());
			return result;
		} else {
			return Collections.emptyMap();
		}
	}

	public List<Command> queryCommands() {
		try {
			String xml = m_config.toString();
			AppConfig config = DefaultSaxParser.parse(xml);

			Map<Integer, Command> commands = config.getCommands();

			for (Entry<Integer, Command> entry : commands.entrySet()) {
				entry.getValue().getCodes().putAll(m_config.getCodes());
			}
			return new ArrayList<Command>(commands.values());
		} catch (Exception e) {
			return new ArrayList<Command>();
		}
	}

	public Map<Integer, Item> queryConfigItem(String name) {
		ConfigItem config = m_config.findConfigItem(name);

		if (config != null) {
			return config.getItems();
		} else {
			return new LinkedHashMap<Integer, Item>();
		}
	}

	public Collection<Item> queryConfigItems(String key) {
		ConfigItem configs = m_config.findConfigItem(key);

		if (configs != null) {
			return configs.getItems().values();
		} else {
			return new ArrayList<Item>();
		}
	}

	public void refreshAppConfigConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AppConfig appConfig = DefaultSaxParser.parse(content);

				m_config = appConfig;
				m_modifyTime = modifyTime;
				refreshData();
			}
		}
	}

	private void refreshData() {
		Collection<Command> commands = m_config.getCommands().values();
		Map<String, Integer> commandMap = new HashMap<String, Integer>();

		for (Command c : commands) {
			commandMap.put(c.getName(), c.getId());
		}
		m_commands = commandMap;

		Map<String, Integer> cityMap = new HashMap<String, Integer>();
		ConfigItem cities = m_config.findConfigItem(CITY);

		for (Item item : cities.getItems().values()) {
			cityMap.put(item.getName(), item.getId());
		}
		m_cities = cityMap;

		Map<String, Integer> operatorMap = new HashMap<String, Integer>();
		ConfigItem operations = m_config.findConfigItem(OPERATOR);

		for (Item item : operations.getItems().values()) {
			operatorMap.put(item.getName(), item.getId());
		}
		m_operators = operatorMap;
	}

	private boolean storeConfig() {
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
		return true;
	}

	public class ConfigReloadTask implements Task {

		@Override
		public String getName() {
			return "AppConfig-Config-Reload";
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					refreshAppConfigConfig();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(60 * 1000L);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
