package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.crash.entity.App;
import com.dianping.cat.configuration.app.crash.entity.CrashLogConfig;
import com.dianping.cat.configuration.app.crash.entity.Modules;
import com.dianping.cat.configuration.app.crash.entity.Server;
import com.dianping.cat.configuration.app.crash.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class CrashLogConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private CrashLogConfig m_config;

	private int m_configId;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "crash-log-config";

	private Map<String, Integer> m_apps;

	public String findServerUrl(String id) {
		Server server = m_config.findServer(id);

		if (server != null) {
			return server.getUrl();
		} else {
			return null;
		}
	}

	public Collection<App> findApps() {
		return m_config.getApps().values();
	}

	public App findApp(String id) {
		return m_config.findApp(id);
	}

	public Modules findModules(String id) {
		return m_config.findModules(id);
	}

	public CrashLogConfig getConfig() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
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
			m_config = new CrashLogConfig();
		}

		buildApps();

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

	private void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				CrashLogConfig appConfig = DefaultSaxParser.parse(content);

				m_config = appConfig;
				m_modifyTime = modifyTime;
				buildApps();
			}
		}
	}
	
	public Integer findAppId(String appName) {
		return m_apps.get(appName);
	}

	private void buildApps() {
		Map<String, Integer> apps = new HashMap<String, Integer>();

		for (App app : m_config.getApps().values()) {
			apps.put(app.getName(), app.getAppId());
		}
		m_apps = apps;
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);

			buildApps();

		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean updateConfig(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}
}
