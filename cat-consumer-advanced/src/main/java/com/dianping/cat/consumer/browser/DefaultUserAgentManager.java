package com.dianping.cat.consumer.browser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.advanced.dal.UserAgent;
import com.dianping.cat.consumer.advanced.dal.UserAgentDao;
import com.dianping.cat.consumer.advanced.dal.UserAgentEntity;

public class DefaultUserAgentManager implements UserAgentManager, LogEnabled {

	private Map<String, UserAgent> m_userAgents = new LinkedHashMap<String, UserAgent>(){
		private static final long serialVersionUID = 1L;
		private int MAX_SIZE = 100000;

		@Override
		protected boolean removeEldestEntry(Entry<String, UserAgent> eldest) {
			return size() > MAX_SIZE;
		}
	};

	@Inject
	private UserAgentDao m_userAgentDao;

	private Logger m_logger;

	@Override
	public void initialize() throws InitializationException {
		try {
			List<UserAgent> userAgents = m_userAgentDao.findAll(UserAgentEntity.READSET_FULL);
			for (UserAgent userAgent : userAgents) {
				m_userAgents.put(userAgent.getBrowser(), userAgent);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}

	}

	@Override
	public UserAgent parse(String userAgentString) {
		if (m_userAgents.containsKey(userAgentString)) {
			return m_userAgents.get(userAgentString);
		} else {
			UserAgentParser uap = null;
			try {
				uap = new UserAgentParser(userAgentString);
			} catch (UserAgentParseException e) {
				m_logger.warn("Can not parse user agent: " + userAgentString, e);
			}
			UserAgent result = m_userAgentDao.createLocal();
			if (uap != null) {
				result.setId(0).setBrowser(uap.getBrowserName()).setVersion(uap.getBrowserVersion())
				      .setOs(uap.getBrowserOperatingSystem()).setUserAgent(uap.getUserAgentString());
			}
			m_userAgents.put(userAgentString, result);
			return result;
		}
	}

	@Override
	public void storeResult() {
		for (UserAgent agent : m_userAgents.values()) {
			if (agent.getId() == 0) {
				try {
					m_userAgentDao.insert(agent);
				} catch (DalException e) {
					m_logger.warn(e.getMessage(), e);
				}

			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
