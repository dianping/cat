package com.dianping.cat.config.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.group.entity.AppCommandGroupConfig;
import com.dianping.cat.configuration.group.entity.SubCommand;
import com.dianping.cat.configuration.group.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class AppCommandGroupConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	private volatile AppCommandGroupConfig m_config;

	private int m_configId;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "app-command-group";

	private volatile Map<String, List<Command>> m_commands = new HashMap<String, List<Command>>();

	public boolean deleteByName(String parent, String subCommand) {
		boolean ret = false;
		com.dianping.cat.configuration.group.entity.Command command = m_config.findCommand(parent);

		if (command != null) {
			command.removeSubCommand(subCommand);

			if (storeConfig()) {
				try {
					refreshData();

					ret = true;
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
		return ret;
	}

	private List<Command> findOrCreate(String id, Map<String, List<Command>> maps) {
		List<Command> list = maps.get(id);

		if (list == null) {
			list = new ArrayList<Command>();

			maps.put(id, list);
		}
		return list;
	}

	public AppCommandGroupConfig getConfig() {
		return m_config;
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
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new AppCommandGroupConfig();
		}

		try {
			refreshData();
		} catch (Exception e) {
			Cat.logError(e);
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

	public boolean insert(String parent, String subCommand) {
		com.dianping.cat.configuration.group.entity.Command command = m_config.findOrCreateCommand(parent);

		command.findOrCreateSubCommand(subCommand);
		return storeConfig();
	}

	public Set<String> queryParentCommands(String id) {
		Set<String> rets = new HashSet<String>();
		List<Command> commands = m_commands.get(id);

		if (commands != null) {
			for (Command command : commands) {
				rets.add(command.getName());
			}
		}
		return rets;
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AppCommandGroupConfig serverConfig = DefaultSaxParser.parse(content);
				m_config = serverConfig;
				m_modifyTime = modifyTime;

				refreshData();
			}
		}
	}

	private void refreshData() throws Exception {
		Map<String, List<Command>> results = new HashMap<String, List<Command>>();
		Map<String, Command> commands = m_appConfigManager.getCommands();

		for (com.dianping.cat.configuration.group.entity.Command cmd : m_config.getCommands().values()) {
			String id = cmd.getId();
			Command parent = commands.get(id);

			if (parent != null) {
				for (Entry<String, SubCommand> entry : cmd.getSubCommands().entrySet()) {
					SubCommand subCmd = entry.getValue();
					String name = subCmd.getId();
					Command c = commands.get(name);

					if (c != null) {
						List<Command> lst = findOrCreate(c.getName(), results);

						lst.add(parent);
					}
				}
			}
		}
		m_commands = results;
	}

	public boolean storeConfig() {
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
		return true;
	}
}
