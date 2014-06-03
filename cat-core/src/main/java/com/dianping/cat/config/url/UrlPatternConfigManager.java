package com.dianping.cat.config.url;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.configuration.url.pattern.entity.UrlPattern;
import com.dianping.cat.configuration.url.pattern.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class UrlPatternConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private UrlPatternHandler m_handler;

	private int m_configId;

	private static final String CONFIG_NAME = "url-pattern";

	private UrlPattern m_UrlPattern;

	private long m_modifyTime;

	public boolean deletePatternItem(String key) {
		m_UrlPattern.removePatternItem(key);
		return storeConfig();
	}

	public String handle(String url) {
		return m_handler.handle(url);
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_UrlPattern = DefaultSaxParser.parse(content);
			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();

		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-url-pattern-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_UrlPattern = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_UrlPattern == null) {
			m_UrlPattern = new UrlPattern();
		}
		m_handler.register(queryUrlPatternRules());
		Threads.forGroup("Cat").start(new ConfigReloadTask());
	}

	public boolean insertPatternItem(PatternItem rule) {
		m_UrlPattern.addPatternItem(rule);
		m_handler.register(queryUrlPatternRules());
		
		return storeConfig();
	}

	public PatternItem queryUrlPattern(String key) {
		return m_UrlPattern.findPatternItem(key);
	}

	public Collection<PatternItem> queryUrlPatternRules() {
		return m_UrlPattern.getPatternItems().values();
	}

	public void refreshUrlPatternConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				UrlPattern pattern = DefaultSaxParser.parse(content);

				m_UrlPattern = pattern;
				m_handler.register(new ArrayList<PatternItem>(m_UrlPattern.getPatternItems().values()));
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
			config.setContent(m_UrlPattern.toString());
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
			return "UrlPattern-Config-Reload";
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					refreshUrlPatternConfig();
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
