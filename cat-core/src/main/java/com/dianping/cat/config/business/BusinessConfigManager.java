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
package com.dianping.cat.config.business;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.transform.DefaultSaxParser;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class BusinessConfigManager extends ContainerHolder implements Initializable {

	public final static String BASE_CONFIG = "base";

	@Inject
	private BusinessConfigDao m_configDao;

	private Map<String, Set<String>> m_domains = new ConcurrentHashMap<String, Set<String>>();

	private Map<String, BusinessReportConfig> m_configs = new ConcurrentHashMap<String, BusinessReportConfig>();

	private boolean m_alertMachine;

	private BusinessItemConfig buildBusinessItemConfig(String key, ConfigItem item) {
		BusinessItemConfig config = new BusinessItemConfig();

		config.setId(key);
		config.setTitle(item.getTitle());
		config.setShowAvg(item.isShowAvg());
		config.setShowCount(item.isShowCount());
		config.setShowSum(item.isShowSum());
		config.setViewOrder(item.getViewOrder());
		return config;
	}

	public boolean deleteBusinessItem(String domain, String key) {
		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain, BusinessConfigEntity.READSET_FULL);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeBusinessItemConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdatetime(new Date());
			m_configDao.updateByPK(config, BusinessConfigEntity.UPDATESET_FULL);

			Set<String> itemIds = m_domains.get(domain);

			itemIds.remove(key);
			cacheConfigs(businessReportConfig, domain);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean deleteCustomItem(String domain, String key) {
		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain, BusinessConfigEntity.READSET_FULL);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeCustomConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdatetime(new Date());

			m_configDao.updateByPK(config, BusinessConfigEntity.UPDATESET_FULL);
			cacheConfigs(businessReportConfig, domain);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}

		return true;
	}

	@Override
	public void initialize() throws InitializationException {
		ServerConfigManager serverConfigManager = lookup(ServerConfigManager.class);
		m_alertMachine = serverConfigManager.isAlertMachine();

		loadData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				loadData();
			}

			@Override
			public String getName() {
				return BASE_CONFIG;
			}
		});
	}

	private void loadData() {
		try {
			List<BusinessConfig> configs = m_configDao.findByName(BASE_CONFIG, BusinessConfigEntity.READSET_FULL);
			Map<String, Set<String>> domains = new ConcurrentHashMap<String, Set<String>>();

			for (BusinessConfig config : configs) {
				try {
					BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());
					String domain = businessReportConfig.getId();
					Set<String> itemIds = new HashSet<String>(businessReportConfig.getBusinessItemConfigs().keySet());

					domains.put(domain, itemIds);
					cacheConfigs(businessReportConfig, domain);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			m_domains = domains;
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void cacheConfigs(BusinessReportConfig businessReportConfig, String domain) {
		if (m_alertMachine) {
			m_configs.put(domain, businessReportConfig);
		}
	}

	public boolean insertBusinessConfigIfNotExist(String domain, String key, ConfigItem item) {
		try {
			if (!m_domains.containsKey(domain)) {
				BusinessReportConfig config = new BusinessReportConfig();
				config.setId(domain);

				BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);
				config.addBusinessItemConfig(businessItemConfig);

				BusinessConfig businessConfig = m_configDao.createLocal();
				businessConfig.setName(BASE_CONFIG);
				businessConfig.setDomain(domain);
				businessConfig.setContent(config.toString());
				businessConfig.setUpdatetime(new Date());
				m_configDao.insert(businessConfig);

				Set<String> itemIds = new HashSet<String>();
				itemIds.add(key);
				m_domains.put(domain, itemIds);
				cacheConfigs(config, domain);
			} else {
				Set<String> itemIds = m_domains.get(domain);

				if (!itemIds.contains(key)) {
					BusinessConfig businessConfig = m_configDao
											.findByNameDomain(BASE_CONFIG, domain,	BusinessConfigEntity.READSET_FULL);
					BusinessReportConfig config = DefaultSaxParser.parse(businessConfig.getContent());
					BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);

					config.addBusinessItemConfig(businessItemConfig);
					businessConfig.setContent(config.toString());
					m_configDao.updateByPK(businessConfig, BusinessConfigEntity.UPDATESET_FULL);

					itemIds.add(key);
					cacheConfigs(config, domain);
				}
			}

			return true;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

	public BusinessReportConfig queryConfigByDomain(String domain) {
		BusinessReportConfig businessReportConfig = null;

		try {
			if (m_alertMachine) {
				businessReportConfig = m_configs.get(domain);
			} else {
				BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain, BusinessConfigEntity.READSET_FULL);

				businessReportConfig = DefaultSaxParser.parse(config.getContent());
			}
		} catch (DalNotFoundException notFound) {
			// Ignore
		} catch (Exception e) {
			Cat.logError(e);
		}

		if (businessReportConfig == null) {
			businessReportConfig = new BusinessReportConfig();
		}
		return businessReportConfig;
	}

	public boolean updateConfigByDomain(BusinessReportConfig config) {
		BusinessConfig proto = m_configDao.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());

		try {
			m_configDao.updateBaseConfigByDomain(proto, BusinessConfigEntity.UPDATESET_FULL);
			cacheConfigs(config, domain);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}

		return false;
	}

	public boolean insertConfigByDomain(BusinessReportConfig config) {
		BusinessConfig proto = m_configDao.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());
		proto.setUpdatetime(new Date());

		try {
			m_configDao.insert(proto);
			cacheConfigs(config, domain);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}

		return false;
	}
}
