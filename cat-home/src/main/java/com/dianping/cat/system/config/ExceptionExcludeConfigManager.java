package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.dependency.exceptionExclude.entity.DomainConfig;
import com.dianping.cat.home.dependency.exceptionExclude.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exceptionExclude.entity.ExceptionExcludeConfig;
import com.dianping.cat.home.dependency.exceptionExclude.transform.DefaultSaxParser;

public class ExceptionExcludeConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private ExceptionExcludeConfig m_exceptionConfig;

	public static String DEFAULT_STRING = "Default";

	public static String ALL_STRING = "All";

	private static final String CONFIG_NAME = "exceptionExcludeConfig";

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_exceptionConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-exception-exclude-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_exceptionConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_exceptionConfig == null) {
			m_exceptionConfig = new ExceptionExcludeConfig();
		}
	}

	public boolean deleteExceptionExclude(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(domain);
		
		domainConfig.removeExceptionExclude(exceptionName);
		return storeConfig();
	}

	public boolean insertExceptionExclude(ExceptionExclude exception) {
		DomainConfig domainConfig = m_exceptionConfig.findOrCreateDomainConfig(exception.getDomain());
		
		domainConfig.getExceptionExcludes().put(exception.getId(), exception);
		return storeConfig();
	}

	public List<ExceptionExclude> queryAllExceptionExcludes() {
		List<ExceptionExclude> result = new ArrayList<ExceptionExclude>();
		
		for (DomainConfig domainConfig : m_exceptionConfig.getDomainConfigs().values()) {
			result.addAll(domainConfig.getExceptionExcludes().values());
		}
		return result;
	}

	public ExceptionExclude queryDomainExceptionExclude(String domain, String exceptionName) {
		DomainConfig domainConfig = m_exceptionConfig.getDomainConfigs().get(domain);
		ExceptionExclude result = null;

		if (domainConfig == null) {
			domainConfig = m_exceptionConfig.getDomainConfigs().get(DEFAULT_STRING);
		}
		if (domainConfig != null) {
			result = domainConfig.getExceptionExcludes().get(exceptionName);
			
			if (result == null) {
				result = domainConfig.getExceptionExcludes().get(ALL_STRING);
			}
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
