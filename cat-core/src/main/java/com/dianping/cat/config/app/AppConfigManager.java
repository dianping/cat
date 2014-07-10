package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.configuration.app.entity.AppConfig;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.ConfigItem;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.entity.Url;
import com.dianping.cat.configuration.app.transform.DefaultSaxParser;

public class AppConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	private int m_configId;

	private static final String CONFIG_NAME = "app-config";

	private AppConfig m_config;

	private long m_modifyTime;

	public static String NETWORK = "网络类型";

	public static String OPERATOR = "运营商";

	public static String VERSION = "版本";

	public static String PLATFORM = "平台";

	public static String CITY = "城市";

	public static String CHANNEL = "渠道";

	public AppConfig getConfig() {
		return m_config;
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
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
		Threads.forGroup("Cat").start(new ConfigReloadTask());
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

	public Collection<Code> queryCodeByCommand(int command) {
		Url url = m_config.findUrl(command);

		if (url != null) {
			return url.getCodes().values();
		} else {
			return null;
		}
	}

	public Collection<Url> queryCommands() {
		return m_config.getUrls().values();
	}

	public ConfigItem queryConfigItem(String name) {
		return m_config.findConfigItem(name);
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
				AppConfig pattern = DefaultSaxParser.parse(content);

				m_config = pattern;
				m_modifyTime = modifyTime;
			}
		}
	}

	private boolean storeConfig() {
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
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(10 * 1000L);
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
