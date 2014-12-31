package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.exception.entity.DomainConfig;
import com.dianping.cat.home.exception.entity.ExceptionConfig;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.transform.DefaultSaxParser;

public class ExceptionConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private ExceptionConfig m_exceptionConfig;

	private static final String CONFIG_NAME = "exceptionConfig";

	public static String DEFAULT_STRING = "Default";

	public static String TOTAL_STRING = "Total";

	public boolean deleteExceptionExclude(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(domain);

		domainConfig.removeExceptionExclude(exceptionName);
		return storeConfig();
	}

	public boolean deleteExceptionLimit(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(domain);
		domainConfig.removeExceptionLimit(exceptionName);
		return storeConfig();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_exceptionConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_exceptionConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_exceptionConfig == null) {
			m_exceptionConfig = new ExceptionConfig();
		}
	}

	public boolean insertExceptionExclude(ExceptionExclude exception) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(exception.getDomain());

		domainConfig.getExceptionExcludes().put(exception.getId(), exception);
		return storeConfig();
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(limit.getDomain());
		domainConfig.getExceptionLimits().put(limit.getId(), limit);

		return storeConfig();
	}

	public List<ExceptionExclude> queryAllExceptionExcludes() {
		List<ExceptionExclude> result = new ArrayList<ExceptionExclude>();

		for (DomainConfig domainConfig : m_exceptionConfig.getDomainConfigs().values()) {
			result.addAll(domainConfig.getExceptionExcludes().values());
		}
		Collections.sort(result, new Comparator<ExceptionExclude>() {

			@Override
			public int compare(ExceptionExclude o1, ExceptionExclude o2) {
				if ("Default".equals(o1.getDomain())) {
					return 1;
				} else {
					return o1.getDomain().compareTo(o2.getDomain());
				}
			}

		});
		return result;
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		List<ExceptionLimit> result = new ArrayList<ExceptionLimit>();

		for (DomainConfig domainConfig : m_exceptionConfig.getDomainConfigs().values()) {
			result.addAll(domainConfig.getExceptionLimits().values());
		}
		Collections.sort(result, new Comparator<ExceptionLimit>() {

			@Override
			public int compare(ExceptionLimit o1, ExceptionLimit o2) {
				if ("Default".equals(o1.getDomain())) {
					return 1;
				} else {
					return o1.getDomain().compareTo(o2.getDomain());
				}
			}

		});
		return result;
	}

	private ExceptionExclude queryDefaultExceptionExclude(String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.getDomainConfigs().get(DEFAULT_STRING);
		ExceptionExclude result = null;

		if (domainConfig != null) {
			result = domainConfig.getExceptionExcludes().get(exceptionName);
		}
		return result;
	}

	public ExceptionExclude queryDomainExceptionExclude(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.getDomainConfigs().get(domain);
		ExceptionExclude result = null;

		// has no this domain config
		if (domainConfig == null) {
			result = queryDefaultExceptionExclude(exceptionName);
		}

		if (domainConfig != null) {
			result = domainConfig.getExceptionExcludes().get(exceptionName);
			// domain config has no exclude for exception, check default exclude config
			if (result == null) {
				result = queryDefaultExceptionExclude(exceptionName);
			}
		}
		return result;
	}

	public ExceptionLimit queryDomainExceptionLimit(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.getDomainConfigs().get(domain);
		ExceptionLimit result = null;

		if (domainConfig == null) {
			domainConfig = m_exceptionConfig.getDomainConfigs().get(DEFAULT_STRING);
		}
		if (domainConfig != null) {
			result = domainConfig.getExceptionLimits().get(exceptionName);
		}
		return result;
	}

	public ExceptionLimit queryDomainTotalLimit(String domain) {
		ExceptionLimit result = queryDomainExceptionLimit(domain, TOTAL_STRING);

		if (result == null) {
			result = queryDomainExceptionLimit(DEFAULT_STRING, TOTAL_STRING);
		}
		return result;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_exceptionConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
