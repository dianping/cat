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
package com.dianping.cat.system.page.permission;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;
import com.dianping.cat.home.resource.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class ResourceConfigManager implements Initializable {

	public static final int DEFAULT_RESOURCE_ROLE = 1;

	private static final String CONFIG_NAME = "resource-config";

	private static final String ALL = "*";

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private ResourceConfig m_config;

	private volatile Map<String, Map<String, Integer>> m_permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

	public ResourceConfig getConfig() {
		return m_config;
	}

	public int getRole(String path, String op) {
		Map<String, Integer> pathPermission = m_permissions.get(path);

		if (pathPermission == null) {
			pathPermission = m_permissions.get(ALL);
		}

		if (pathPermission != null) {
			Integer role = pathPermission.get(op);

			if (role == null) {
				role = pathPermission.get(ALL);
			}

			if (role != null) {
				return role;
			}
		}

		return DEFAULT_RESOURCE_ROLE;
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
			m_config = new ResourceConfig();
		}
		refreshData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

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
				ResourceConfig resourceConfig = DefaultSaxParser.parse(content);
				m_config = resourceConfig;
				m_modifyTime = modifyTime;

				refreshData();
			}
		}
	}

	private void refreshData() {
		Map<String, Map<String, Integer>> permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

		for (Resource resource : m_config.getResources()) {
			String path = resource.getPath();
			Map<String, Integer> pathPermission = permissions.get(path);

			if (pathPermission == null) {
				pathPermission = new ConcurrentHashMap<String, Integer>();
				permissions.put(path, pathPermission);
			}

			pathPermission.put(resource.getOp(), resource.getRole());
		}

		m_permissions = permissions;
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

				refreshData();
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
