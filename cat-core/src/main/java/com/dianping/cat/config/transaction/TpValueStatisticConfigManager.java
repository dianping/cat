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
package com.dianping.cat.config.transaction;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;
import com.dianping.cat.configuration.tp.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;

@Named
public class TpValueStatisticConfigManager implements Initializable {

	public static final String DEFAULT = "default";

	private static final String CONFIG_NAME = "tp-value-statistic-config";

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	private int m_configId;

	private long m_modifyTime;

	private TpValueStatisticConfig m_config;

	public TpValueStatisticConfig getConfig() {
		return m_config;
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
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new TpValueStatisticConfig();
		}

		TimerSyncTask.getInstance().register(new TimerSyncTask.SyncHandler() {

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
				TpValueStatisticConfig tmp = DefaultSaxParser.parse(content);

				m_config = tmp;
				m_modifyTime = modifyTime;
			}
		}
	}

	private boolean defaultContainsType(String type) {
		Domain d = m_config.findDomain("default");
		return d.getTransactionTypes().contains(type);
	}

	private boolean domainContainsType(String type, String domain) {
		Domain d = m_config.findDomain(domain);
		return d != null && d.getTransactionTypes().contains(type);
	}

	private boolean matchesPrefix(String type) {
		for (String prefix : m_serverConfigManager.getForcedStatisticTypePrefixes()) {
			if (type.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public boolean shouldStatistic(String type, String domain) {
		try {
			return defaultContainsType(type) || matchesPrefix(type) || domainContainsType(type, domain);
		} catch (Exception e) {
			Cat.logError("no default config in tp9xx config: " + m_config.toString(), e);
			return false;
		}
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
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public Set<String> findTransactionTypesByDomain(String domain) {
		Domain d = m_config.findDomain(domain);

		if (d != null) {
			return d.getTransactionTypes();
		} else {
			return new HashSet<String>();
		}
	}

	public Set<String> deleteByDomainType(String domain, String type) {
		Set<String> types = findTransactionTypesByDomain(domain);

		if (types.contains(type)) {
			types.remove(type);
			storeConfig();
			return types;
		}
		return types;
	}

	public void insertOrUpdateByDomain(String domain, Set<String> params) {
		Domain dd = m_config.findDomain(domain);
		Set<String> st = null;

		if (dd == null) {
			dd = new Domain();
			dd.setId(domain);
		}
		st = dd.getTransactionTypes();
		st.clear();
		st.addAll(params);

		m_config.addDomain(dd);
		storeConfig();
	}
}
