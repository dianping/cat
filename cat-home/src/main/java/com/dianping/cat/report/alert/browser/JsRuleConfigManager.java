package com.dianping.cat.report.alert.browser;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.js.transform.DefaultSaxParser;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import com.dianping.cat.home.js.entity.JsRuleConfig;
import com.site.lookup.util.StringUtils;

@Named
public class JsRuleConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private JsRuleConfig m_jsRuleConfig;

	private static final String CONFIG_NAME = "jsRuleConfig";

	private static String DEFAULT_STRING = "Default";

	public static final String SPLITTER = ":";

	public boolean deleteExceptionLimit(String ruleId) {
		m_jsRuleConfig.removeExceptionLimit(ruleId);

		return storeConfig();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			m_configId = config.getId();
			m_jsRuleConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_jsRuleConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_jsRuleConfig == null) {
			m_jsRuleConfig = new JsRuleConfig();
		}
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		String id = limit.getId();

		if (StringUtils.isEmpty(id)) {
			id = limit.getDomain() + SPLITTER + limit.getLevel();
			limit.setId(id);
		}

		m_jsRuleConfig.getExceptionLimits().put(id, limit);
		return storeConfig();
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		return new ArrayList<ExceptionLimit>(m_jsRuleConfig.getExceptionLimits().values());
	}

	public ExceptionLimit queryExceptionLimit(String domain, String level) {
		ExceptionLimit exceptionLimit = m_jsRuleConfig.findExceptionLimit(domain + SPLITTER + level);

		if (exceptionLimit == null) {
			exceptionLimit = m_jsRuleConfig.findExceptionLimit(DEFAULT_STRING + SPLITTER + level);
		}
		return exceptionLimit;
	}

	public ExceptionLimit queryExceptionLimit(String ruleId) {
		return m_jsRuleConfig.findExceptionLimit(ruleId);
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_jsRuleConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
