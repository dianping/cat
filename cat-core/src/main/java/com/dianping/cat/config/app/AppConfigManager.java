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
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
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

	@Inject
	private ContentFetcher m_fetcher;

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

	public Pair<Boolean, Integer> addCommand(String domain, String title, String name, String type) throws Exception {
		Command command = new Command();

		command.setDomain(domain);
		command.setTitle(title);
		command.setName(name);

		int commandId = 0;

		if ("activity".equals(type)) {
			commandId = findAvailableId(1100, 1200);
		} else {
			commandId = findAvailableId(1, 1099);
		}
		command.setId(commandId);

		m_config.addCommand(command);
		return new Pair<Boolean, Integer>(storeConfig(), commandId);
	}

	public boolean containCommand(int id) {
		Set<Integer> keys = m_config.getCommands().keySet();

		if (keys.contains(id)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean deleteCode(int id, int codeId) {
		Command command = m_config.getCommands().get(id);

		if (command != null) {
			command.getCodes().remove(codeId);
		}
		return storeConfig();
	}

	public boolean deleteCommand(int id) {
		m_config.removeCommand(id);
		return storeConfig();
	}

	public Map<Integer, Code> queryInternalCodes(int commandId) {
		Command cmd = m_config.getCommands().get(commandId);

		if (cmd != null) {
			return cmd.getCodes();
		}
		return new HashMap<Integer, Code>();
	}

	public Pair<Boolean, List<Integer>> deleteCommand(String domain, String name) {
		Collection<Command> commands = m_config.getCommands().values();
		List<Integer> needDeleteIds = new ArrayList<Integer>();

		for (Command command : commands) {
			if (name.equals(command.getName())) {
				if (domain == null || (domain != null && domain.equals(command.getDomain()))) {
					needDeleteIds.add(command.getId());
				}
			}
		}
		for (int id : needDeleteIds) {
			m_config.removeCommand(id);
		}
		return new Pair<Boolean, List<Integer>>(storeConfig(), needDeleteIds);
	}

	private int findAvailableId(int startIndex, int endIndex) throws Exception {
		Set<Integer> keys = m_config.getCommands().keySet();
		int maxKey = 0;

		for (int key : keys) {
			if (key >= startIndex && key <= endIndex && key > maxKey) {
				maxKey = key;
			}
		}
		if (maxKey < endIndex && maxKey >= startIndex) {
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

	public Map<Integer, Code> getCodes() {
		return m_config.getCodes();
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

	public boolean isNameDuplicate(String name) {
		for (Command command : m_config.getCommands().values()) {
			if (command.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSuccessCode(int commandId, int code) {
		Map<Integer, Code> codes = queryCodeByCommand(commandId);

		for (Code c : codes.values()) {
			if (c.getId() == code) {
				return (c.getStatus() == 0);
			}
		}
		return false;
	}

	public Map<Integer, Code> queryCodeByCommand(int command) {
		Command c = m_config.findCommand(command);

		if (c != null) {
			Map<Integer, Code> result = new HashMap<Integer, Code>();
			Map<Integer, Code> values = c.getCodes();

			result.putAll(m_config.getCodes());
			result.putAll(values);
			return result;
		} else {
			return Collections.emptyMap();
		}
	}

	public List<Command> queryCommands() {
		try {
//			String xml = m_config.toString();
//			AppConfig config = DefaultSaxParser.parse(xml);
			Map<Integer, Command> commands = m_config.getCommands();

			for (Entry<Integer, Command> entry : commands.entrySet()) {
				Map<Integer, Code> codes = entry.getValue().getCodes();

				for (Entry<Integer, Code> e : m_config.getCodes().entrySet()) {
					if (!codes.containsKey(e.getKey())) {
						codes.put(e.getKey(), e.getValue());
					}
				}
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

		if (cities != null && cities.getItems() != null) {
			for (Item item : cities.getItems().values()) {
				cityMap.put(item.getName(), item.getId());
			}
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

	public boolean updateCode(int id, Code code) {
		Command command = m_config.findCommand(id);

		if (command != null) {
			command.getCodes().put(code.getId(), code);

			return storeConfig();
		}
		return false;
	}

	public boolean updateCommand(int id, String domain, String name, String title) {
		Command command = m_config.findCommand(id);

		command.setDomain(domain);
		command.setName(name);
		command.setTitle(title);
		return storeConfig();
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
