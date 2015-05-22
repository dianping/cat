package com.dianping.cat.config.web.url;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.configuration.web.url.entity.UrlPattern;
import com.dianping.cat.configuration.web.url.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class UrlPatternConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private UrlPatternHandler m_handler;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private volatile UrlPattern m_urlPattern;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "url-pattern";

	private Map<String, PatternItem> m_format2Items = new ConcurrentHashMap<String, PatternItem>();

	public boolean deletePatternItem(String key) {
		m_urlPattern.removePatternItem(key);
		return storeConfig();
	}

	public PatternItem handle(String url) {
		String pattern = m_handler.handle(url);

		return m_format2Items.get(pattern);
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			m_configId = config.getId();
			m_urlPattern = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_urlPattern = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_urlPattern == null) {
			m_urlPattern = new UrlPattern();
		}
		m_handler.register(queryUrlPatternRules());
		refreshData();
		Threads.forGroup("cat").start(new ConfigReloadTask());
	}

	public boolean insertPatternItem(PatternItem rule) {
		m_urlPattern.addPatternItem(rule);
		m_handler.register(queryUrlPatternRules());

		return storeConfig();
	}

	public boolean isSuccessCode(int code) {
		Code c = m_urlPattern.getCodes().get(code);

		if (c != null) {
			return c.getStatus() == 0;
		} else {
			return false;
		}
	}

	public Map<Integer, Code> queryCodes() {
		return m_urlPattern.getCodes();
	}

	public PatternItem queryUrlPattern(String key) {
		return m_urlPattern.findPatternItem(key);
	}

	public Collection<PatternItem> queryUrlPatternRules() {
		return m_urlPattern.getPatternItems().values();
	}

	public Map<String, PatternItem> queryUrlPatterns() {
		return m_urlPattern.getPatternItems();
	}

	public void refreshData() {
		Map<String, PatternItem> items = new HashMap<String, PatternItem>();

		for (PatternItem item : m_urlPattern.getPatternItems().values()) {
			items.put(item.getPattern(), item);
		}
		m_format2Items = items;
	}

	public void refreshUrlPatternConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				UrlPattern pattern = DefaultSaxParser.parse(content);

				m_urlPattern = pattern;
				m_handler.register(queryUrlPatternRules());
				refreshData();

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
			config.setContent(m_urlPattern.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			refreshData();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean updateCode(int id, Code code) {
		m_urlPattern.getCodes().put(code.getId(), code);
		return storeConfig();
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
