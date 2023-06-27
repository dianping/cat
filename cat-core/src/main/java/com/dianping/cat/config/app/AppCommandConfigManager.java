package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.command.entity.AppCommandConfig;
import com.dianping.cat.command.entity.Code;
import com.dianping.cat.command.entity.Codes;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.command.transform.DefaultSaxParser;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class AppCommandConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private volatile Map<String, Command> m_commands = new ConcurrentHashMap<String, Command>();

	private int m_configId;

	private volatile AppCommandConfig m_config;

	private long m_modifyTime;

	private int m_maxCommandId;

	private static final String CONFIG_NAME = "app-command-config";

	public static final String DEFAULT_NAMESPACE = "点评主APP";

	public static final int ALL_COMMAND_ID = 0;

	public static final int SUCCESS_STATUS = 0;

	public boolean addCode(String namespace, Code code) {
		m_config.findOrCreateCodes(namespace).addCode(code);

		return storeConfig();
	}

	public Pair<Boolean, Integer> addCommand(Command command) throws Exception {
		if (!containCommand(command.getName())) {
			int commandId = 0;

			commandId = findAvailableId(1, m_maxCommandId);
			command.setId(commandId);
			m_config.addCommand(command);

			return new Pair<Boolean, Integer>(storeConfig(), commandId);
		} else {
			return new Pair<Boolean, Integer>(false, -1);
		}
	}

	public Map<String, AppCommandInfo> buildNamespace2Commands() {
		return buildNamespace2Commands(queryCommands());
	}

	private Map<String, AppCommandInfo> buildNamespace2Commands(Map<Integer, Command> commands) {
		Map<String, AppCommandInfo> results = new LinkedHashMap<String, AppCommandInfo>();

		for (Command command : commands.values()) {
			String domain = command.getDomain();

			if (StringUtils.isEmpty(domain)) {
				domain = "default";
			}

			String namespace = command.getNamespace();
			AppCommandInfo appCommandDisplayInfo = results.get(namespace);

			if (appCommandDisplayInfo == null) {
				appCommandDisplayInfo = new AppCommandInfo();

				results.put(namespace, appCommandDisplayInfo);
			}
			appCommandDisplayInfo.addCommand(domain, command);
		}
		return results;
	}

	public boolean containCommand(int id) {
		return m_config.getCommands().containsKey(id);
	}

	public boolean containCommand(String name) {
		return m_commands.containsKey(name);
	}

	private AppCommandConfig copyAppCommandConfig() throws SAXException, IOException {
		String xml = m_config.toString();
		AppCommandConfig config = DefaultSaxParser.parse(xml);

		return config;
	}

	public boolean deleteCode(int id, int codeId) {
		Command command = m_config.getCommands().get(id);

		if (command != null) {
			command.getCodes().remove(codeId);
		}
		return storeConfig();
	}

	public boolean deleteCode(String namespace, int codeId) {
		Codes codes = m_config.findCodes(namespace);

		if (codes != null) {
			codes.removeCode(codeId);
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

	public int findAvailableId(int start, int end) throws Exception {
		List<Integer> keys = new ArrayList<Integer>(m_config.getCommands().keySet());
		Collections.sort(keys);
		List<Integer> tmp = new ArrayList<Integer>();

		for (int i = 0; i < keys.size(); i++) {
			int value = keys.get(i);

			if (value >= start && value <= end) {
				tmp.add(value);
			}
		}
		int size = tmp.size();

		if (size == 0) {
			return start;
		} else if (size == 1) {
			return tmp.get(0) + 1;
		} else if (size == end - start + 1) {
			Exception ex = new RuntimeException();
			Cat.logError("app config range is full: " + start + " - " + end, ex);
			throw ex;
		} else {
			int key = tmp.get(0), i = 0;
			int last = key;

			for (; i < size; i++) {
				key = tmp.get(i);

				if (key - last > 1) {
					return last + 1;
				}
				last = key;
			}
			return last + 1;
		}
	}

	public Map<String, Codes> getCodes() {
		return m_config.getCodeses();
	}

	public Map<String, Command> getCommands() {
		return m_commands;
	}

	public AppCommandConfig getConfig() {
		return m_config;
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
				refreshData();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new AppCommandConfig();
		}

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

	public boolean isBusinessSuccessCode(int commandId, int code) {
		Map<Integer, Code> codes = queryCodeByCommand(commandId);

		for (Code c : codes.values()) {
			if (c.getId() == code) {
				return (c.getBusinessStatus() == SUCCESS_STATUS);
			}
		}
		return false;
	}

	public boolean isNameDuplicate(String name) {
		return m_commands.containsKey(name);
	}

	public boolean isSuccessCode(int commandId, int code) {
		Map<Integer, Code> codes = queryCodeByCommand(commandId);

		for (Code c : codes.values()) {
			if (c.getId() == code) {
				return (c.getNetworkStatus() == SUCCESS_STATUS);
			}
		}
		return false;
	}

	public Map<Integer, Code> queryCodeByCommand(int command) {
		Command c = m_config.findCommand(command);
		Map<Integer, Code> result = new HashMap<Integer, Code>();
		Codes codes = m_config.findCodes(c.getNamespace());

		if (codes == null) {
			codes = m_config.findCodes(DEFAULT_NAMESPACE);
		}
		result.putAll(codes.getCodes());

		if (c != null) {
			Map<Integer, Code> values = c.getCodes();
			result.putAll(values);
		}
		return result;
	}

	public Map<Integer, List<Code>> queryCommand2Codes() {
		Map<Integer, List<Code>> codes = new LinkedHashMap<Integer, List<Code>>();

		for (Command command : m_config.getCommands().values()) {
			List<Code> items = codes.get(command.getId());

			if (items == null) {
				items = new ArrayList<Code>();
				codes.put(command.getId(), items);
			}
			items.addAll(command.getCodes().values());
		}
		return codes;
	}

	public Map<Integer, Command> queryCommands() {
		try {
			AppCommandConfig config = copyAppCommandConfig();
			Map<Integer, Command> commands = config.getCommands();

			commands = SortHelper.sortMap(commands, new Comparator<Entry<Integer, Command>>() {
				@Override
				public int compare(Entry<Integer, Command> o1, Entry<Integer, Command> o2) {
					String c1 = o1.getValue().getName();
					String title1 = o1.getValue().getTitle();
					String c2 = o2.getValue().getName();
					String title2 = o2.getValue().getTitle();

					if (StringUtils.isNotEmpty(title1)) {
						c1 = title1;
					}

					if (StringUtils.isNotEmpty(title2)) {
						c2 = title2;
					}

					return c1.compareTo(c2);
				}
			});

			return commands;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return Collections.emptyMap();
	}

	public List<String> queryDuplicateNames(List<String> names) {
		List<String> results = new ArrayList<String>();

		for (String name : names) {
			if (m_commands.containsKey(name)) {
				results.add(name);
			}
		}
		return results;
	}

	public Map<Integer, Code> queryInternalCodes(int commandId) {
		Command cmd = m_config.getCommands().get(commandId);

		if (cmd != null) {
			return cmd.getCodes();
		}
		return new HashMap<Integer, Code>();
	}

	public Map<String, List<Command>> queryNamespace2Commands() {
		Map<String, List<Command>> results = new HashMap<String, List<Command>>();

		for (Entry<Integer, Command> entry : m_config.getCommands().entrySet()) {
			Command command = entry.getValue();
			String namespace = command.getNamespace();
			List<Command> commands = results.get(namespace);

			if (commands == null) {
				commands = new LinkedList<Command>();

				results.put(namespace, commands);
			}
			commands.add(command);
		}
		return results;
	}

	private void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AppCommandConfig appConfig = DefaultSaxParser.parse(content);

				m_config = appConfig;
				m_modifyTime = modifyTime;
				refreshData();
			}
		}
	}

	private void refreshData() {
		m_maxCommandId = m_config.getMaxCommandId();

		Collection<Command> commands = m_config.getCommands().values();
		Map<String, Command> commandMap = new ConcurrentHashMap<String, Command>();

		for (Command c : commands) {
			commandMap.put(c.getName(), c);
		}
		m_commands = commandMap;
	}

	private void sortCommands() {
		Map<Integer, Command> commands = m_config.getCommands();
		Map<Integer, Command> results = new LinkedHashMap<Integer, Command>();
		List<Integer> ids = new ArrayList<Integer>(commands.keySet());
		Collections.sort(ids);

		for (int i = 0; i < ids.size(); i++) {
			int id = ids.get(i);

			results.put(id, commands.get(id));
		}
		synchronized (this) {
			commands.clear();
			commands.putAll(results);
		}
	}

	public boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);

			sortCommands();
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

	public boolean updateCode(String namespace, Code code) {
		m_config.findCodes(namespace).addCode(code);

		return true;
	}

	public boolean updateCommand(int id, Command command) {
		Command c = m_config.findCommand(id);

		c.setDomain(command.getDomain());
		c.setName(command.getName().toLowerCase());
		c.setTitle(command.getTitle());
		c.setThreshold(command.getThreshold());
		c.setNamespace(command.getNamespace());
		return storeConfig();
	}

	public static class AppCommandInfo {

		private Map<String, List<Command>> m_commands = new HashMap<String, List<Command>>();

		public AppCommandInfo() {

		}

		public AppCommandInfo(Map<String, List<Command>> commands) {
			m_commands = commands;
		}

		public void addCommand(String domain, Command command) {
			List<Command> commands = m_commands.get(domain);

			if (commands == null) {
				commands = new ArrayList<Command>();

				m_commands.put(domain, commands);
			}
			commands.add(command);
		}

		public Map<String, List<Command>> getCommands() {
			return m_commands;
		}

		@Override
		public String toString() {
			return "AppCommandDisplayInfo [m_commands=" + m_commands + "]";
		}

	}
}
