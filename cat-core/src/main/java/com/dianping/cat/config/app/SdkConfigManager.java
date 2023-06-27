package com.dianping.cat.config.app;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.sdk.entity.App;
import com.dianping.cat.configuration.app.sdk.entity.SdkConfig;
import com.dianping.cat.configuration.app.sdk.entity.Type;
import com.dianping.cat.configuration.app.sdk.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class SdkConfigManager implements Initializable, LogEnabled {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private Logger m_logger;

	private int m_configId;

	private long m_modifyTime;

	private volatile SdkConfig m_config;

	private static final String CONFIG_NAME = "sdk-config";

	private static final String DEFAULT = "default";

	private Map<String, Map<String, Boolean>> m_datas;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public SdkConfig getConfig() {
		return m_config;
	}

	public Map<String, Boolean> getConfig(String appId) {
		Map<String, Boolean> data = m_datas.get(appId);

		if (data == null) {
			data = m_datas.get(DEFAULT);
		}

		return data;
	}

	public Boolean getConfig(String appId, String type) {
		Map<String, Boolean> data = m_datas.get(appId);

		if (data == null) {
			data = m_datas.get(DEFAULT);
		}

		Boolean result = data.get(type);

		if (result == null) {
			result = true;
		}

		return result;
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
			m_config = new SdkConfig();
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

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				SdkConfig MobileConfig = DefaultSaxParser.parse(content);
				m_config = MobileConfig;
				m_modifyTime = modifyTime;

				refreshData();
			}
		}
	}

	private void refreshData() {
		Map<String, Map<String, Boolean>> datas = new HashMap<String, Map<String, Boolean>>();
		Map<String, App> apps = m_config.getApps();

		for (App app : apps.values()) {
			Map<String, Boolean> data = new HashMap<String, Boolean>();
			Map<String, Type> types = app.getTypes();

			for (Type type : types.values()) {
				data.put(type.getId(), type.getEnable());
			}

			datas.put(app.getId(), data);
		}

		m_datas = datas;
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
