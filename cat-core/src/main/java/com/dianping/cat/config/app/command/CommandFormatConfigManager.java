package com.dianping.cat.config.app.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.command.entity.Command;
import com.dianping.cat.configuration.app.command.entity.CommandFormat;
import com.dianping.cat.configuration.app.command.entity.Rule;
import com.dianping.cat.configuration.app.command.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named(type = CommandFormatConfigManager.class)
public class CommandFormatConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected CommandFormatHandler m_handler;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private static final String CONFIG_NAME = "app-command-format-config";

	private volatile CommandFormat m_urlFormat;

	private Map<String, Rule> m_map = new HashMap<String, Rule>();

	private long m_modifyTime;

	public static final int PROBLEM_TYPE = 3;

	private String buildKey(int type, String pattern) {
		return type + ":" + pattern;
	}

	public CommandFormat getUrlFormat() {
		return m_urlFormat;
	}

	public List<String> handle(int type, String command) {
		String format = m_handler.handle(type, command);
		String key = buildKey(type, format);
		List<String> result = new ArrayList<String>();
		Rule rule = m_map.get(key);

		if (rule != null) {
			for (Command c : rule.getCommands()) {
				result.add(c.getId());
			}
		}

		if (result.isEmpty()) {
			result.add(command);
		}
		return result;
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);

			m_configId = config.getId();
			// no need for refresh
			// refreshData(config);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				// no need for refresh
				// refreshData(config);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_urlFormat == null) {
			m_urlFormat = new CommandFormat();
		}

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

			@Override
			public String getName() {
				return CONFIG_NAME;
			}
		});
	}

	public boolean insert(String xml) {
		try {
			m_urlFormat = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();

			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void refreshData(Config config) throws SAXException, IOException {
		Map<String, Rule> map = new HashMap<String, Rule>();
		String content = config.getContent();
		long modifyTime = config.getModifyDate().getTime();
		CommandFormat format = DefaultSaxParser.parse(content);

		for (Rule rule : format.getRules()) {
			int type = rule.getType();
			String pattern = rule.getPattern();
			String key = buildKey(type, pattern);

			map.put(key, rule);
		}

		m_map = map;
		m_urlFormat = format;
		m_handler.register(m_urlFormat.getRules());
		m_modifyTime = modifyTime;
	}

	private void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				refreshData(config);
			}
		}
	}

	public void setConfigDao(ConfigDao configDao) {
		m_configDao = configDao;
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_urlFormat.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}
}
