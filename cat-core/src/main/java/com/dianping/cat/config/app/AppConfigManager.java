package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.unidal.lookup.util.StringUtils;
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

	public static final int TOO_LONG_COMMAND_ID = 23;

	public static final int ALL_COMMAND_ID = 0;

	public static final int COMMAND_END_INDEX = 1099;

	public static final int ACTIVITY_END_INDEX = 1200;

	public boolean addConstant(String type, int id, String value) {
		ConfigItem item = m_config.getConfigItems().get(type);

		if (item != null) {
			Item data = item.getItems().get(id);

			if (data != null) {
				data.setName(value);
			} else {
				data = new Item(id);
				data.setName(value);

				item.getItems().put(id, data);
			}
			return true;
		} else {
			return false;
		}
	}

	public Item queryItem(String type, int id) {
		ConfigItem itemConfig = m_config.getConfigItems().get(type);

		if (itemConfig != null) {
			Item item = itemConfig.getItems().get(id);

			return item;
		}
		return null;
	}

	public Pair<Boolean, Integer> addCommand(String domain, String title, String name, String type) throws Exception {
		Command command = new Command();

		command.setDomain(domain);
		command.setTitle(title);
		command.setName(name);

		int commandId = 0;

		if ("activity".equals(type)) {
			commandId = findAvailableId(COMMAND_END_INDEX + 1, ACTIVITY_END_INDEX);
		} else {
			commandId = findAvailableId(1, COMMAND_END_INDEX);
		}
		command.setId(commandId);

		m_config.addCommand(command);
		return new Pair<Boolean, Integer>(storeConfig(), commandId);
	}

	private Map<String, List<Command>> buildSortedCommands(Map<String, List<Command>> commands) {
		Map<String, List<Command>> results = new LinkedHashMap<String, List<Command>>();
		List<String> domains = new ArrayList<String>(commands.keySet());

		Collections.sort(domains);
		CommandComparator comparator = new CommandComparator();

		for (String domain : domains) {
			List<Command> cmds = commands.get(domain);

			Collections.sort(cmds, comparator);
			results.put(domain, cmds);
		}

		return results;
	}

	public boolean containCommand(int id) {
		Set<Integer> keys = m_config.getCommands().keySet();

		if (keys.contains(id)) {
			return true;
		} else {
			return false;
		}
	}

	private AppConfig copyAppConfig() throws SAXException, IOException {
		String xml = m_config.toString();
		AppConfig config = DefaultSaxParser.parse(xml);

		return config;
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
		Map<Integer, Code> result = new HashMap<Integer, Code>();

		if (c != null) {
			Map<Integer, Code> values = c.getCodes();

			result.putAll(m_config.getCodes());
			result.putAll(values);
		}
		return result;
	}

	public Map<Integer, List<Code>> queryCommand2Codes() {
		Map<Integer, List<Code>> codes = new LinkedHashMap<Integer, List<Code>>();

		for (Command command : queryCommands()) {
			List<Code> items = codes.get(command.getId());

			if (items == null) {
				items = new ArrayList<Code>();
				codes.put(command.getId(), items);
			}
			items.addAll(command.getCodes().values());
		}
		return codes;
	}

	public List<Command> queryCommands() {
		ArrayList<Command> results = new ArrayList<Command>();

		try {
			AppConfig config = copyAppConfig();
			Map<Integer, Command> commands = config.getCommands();

			for (Entry<Integer, Command> entry : commands.entrySet()) {
				Map<Integer, Code> codes = entry.getValue().getCodes();

				for (Entry<Integer, Code> e : m_config.getCodes().entrySet()) {
					if (!codes.containsKey(e.getKey())) {
						codes.put(e.getKey(), e.getValue());
					}
				}
			}
			results = new ArrayList<Command>(commands.values());
			Collections.sort(results, new CommandComparator());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return results;
	}

	public List<Command> queryCommands(boolean activity) {
		List<Command> commands = queryCommands();
		List<Command> results = new ArrayList<Command>();

		if (activity) {
			for (Command command : commands) {
				int commandId = command.getId();
				if (commandId > COMMAND_END_INDEX && commandId <= ACTIVITY_END_INDEX) {
					results.add(command);
				}
			}
		} else {
			for (Command command : commands) {
				int commandId = command.getId();
				if (commandId >= ALL_COMMAND_ID && commandId <= COMMAND_END_INDEX) {
					results.add(command);
				}
			}
		}
		return results;
	}

	public Map<Integer, Item> queryConfigItem(String name) {
		ConfigItem config = m_config.findConfigItem(name);

		if (config != null) {
			return config.getItems();
		} else {
			return new LinkedHashMap<Integer, Item>();
		}
	}

	public Map<String, List<Command>> queryDomain2Commands() {
		return queryDomain2Commands(queryCommands());
	}

	public Map<String, List<Command>> queryDomain2Commands(boolean activity) {
		return queryDomain2Commands(queryCommands(activity));
	}

	public Map<String, List<Command>> queryDomain2Commands(List<Command> commands) {
		Map<String, List<Command>> map = new LinkedHashMap<String, List<Command>>();

		for (Command command : commands) {
			String domain = command.getDomain();

			if (StringUtils.isEmpty(domain)) {
				domain = "default";
			}
			List<Command> cmds = map.get(domain);

			if (cmds == null) {
				cmds = new ArrayList<Command>();
				map.put(domain, cmds);
			}
			cmds.add(command);
		}
		return buildSortedCommands(map);
	}

	public Map<Integer, Code> queryInternalCodes(int commandId) {
		Command cmd = m_config.getCommands().get(commandId);

		if (cmd != null) {
			return cmd.getCodes();
		}
		return new HashMap<Integer, Code>();
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

	public boolean storeConfig() {
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

	public boolean updateCode(Code code) {
		m_config.getCodes().put(code.getId(), code);

		return true;
	}

	public boolean updateCommand(int id, String domain, String name, String title) {
		Command command = m_config.findCommand(id);

		command.setDomain(domain);
		command.setName(name);
		command.setTitle(title);
		return storeConfig();
	}

	public static class CommandComparator implements Comparator<Command> {

		@Override
		public int compare(Command o1, Command o2) {
			String c1 = o1.getName();
			String title1 = o1.getTitle();
			String c2 = o2.getName();
			String title2 = o2.getTitle();

			if (StringUtils.isNotEmpty(title1)) {
				c1 = title1;
			}

			if (StringUtils.isNotEmpty(title2)) {
				c2 = title2;
			}
			return c1.compareTo(c2);
		}
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
