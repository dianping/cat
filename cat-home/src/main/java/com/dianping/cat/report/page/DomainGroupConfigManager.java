/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page;

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
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.home.group.transform.DefaultSaxParser;

@Named
public class DomainGroupConfigManager implements Initializable {

	private static final String CONFIG_NAME = "domainGroup";

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private DomainGroup m_domainGroup;

	public DomainGroup getDomainGroup() {
		return m_domainGroup;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_domainGroup = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_domainGroup = DefaultSaxParser.parse(content);
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

	public boolean insertFromJson(String json) {
		try {
			Domain domain = (Domain) new JsonBuilder().parse(json, Domain.class);

			m_domainGroup.addDomain(domain);
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public boolean deleteGroup(String domain) {
		m_domainGroup.removeDomain(domain);

		return storeConfig();
	}

	public String queryDefaultGroup(String domain) {
		List<String> groups = queryDomainGroup(domain);

		if (groups.size() >= 1) {
			return groups.get(0);
		} else {
			return "";
		}
	}

	public Domain queryGroupDomain(String domain) {
		Domain domainGroup = m_domainGroup.findDomain(domain);

		return domainGroup;
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
