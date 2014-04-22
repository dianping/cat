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
import com.dianping.cat.home.domainGroup.entity.Domain;
import com.dianping.cat.home.domainGroup.entity.DomainGroup;
import com.dianping.cat.home.domainGroup.entity.Group;
import com.dianping.cat.home.domainGroup.transform.DefaultSaxParser;

public class DomainGroupConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private DomainGroup m_domainGroup;

	private static final String CONFIG_NAME = "domainGroup";

	public DomainGroup getDomainGroup() {
		return m_domainGroup;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_domainGroup = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-domain-group-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_domainGroup = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_domainGroup == null) {
			m_domainGroup = new DomainGroup();
		}
	}

	public boolean insert(String xml) {
		try {
			m_domainGroup = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public String queryDefaultGroup(String domain) {
		List<String> groups = queryDomainGroup(domain);

		if (groups.size() >= 1) {
			return groups.get(0);
		} else {
			return "";
		}
	}

	public List<String> queryDomainGroup(String domain) {
		Domain domainGroup = m_domainGroup.findDomain(domain);

		if (domainGroup == null) {
			return new ArrayList<String>();
		} else {
			return new ArrayList<String>(domainGroup.getGroups().keySet());
		}
	}

	public List<String> queryIpByDomainAndGroup(String domain, String group) {
		Domain domainInfo = m_domainGroup.findDomain(domain);

		if (domainInfo != null) {
			Group groupInfo = domainInfo.findGroup(group);

			if (groupInfo != null) {
				return groupInfo.getIps();
			}
		}
		return new ArrayList<String>();
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_domainGroup.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
