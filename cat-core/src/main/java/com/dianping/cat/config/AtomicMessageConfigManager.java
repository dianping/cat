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
package com.dianping.cat.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;
import com.dianping.cat.configuration.message.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;

@Named
public class AtomicMessageConfigManager implements Initializable {

	private static final String CONFIG_NAME = "atomic-message-config";

	private static final String DEFAULT_DOMAIN = "default";

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private AtomicMessageConfig m_config;

	public AtomicMessageConfig getConfig() {
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
			m_config = new AtomicMessageConfig();
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

	public String queryAtomicMatchTypes(String domain) {
		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getMatchTypes();
		} else {
			return "";
		}
	}

	public String queryAtomicStartTypes(String domain) {
		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getStartTypes();
		} else {
			return "";
		}
	}

	public String queryMaxMetricTagValues(String domain) {
		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			Property property = d.findProperty("max-metric-tagvalues");

			if (property != null) {
				return property.getValue();
			}
		}

		return "10000";
	}

	public int getPropertyValue(String domain, String propertyName, int defaultValue) {
		int result = defaultValue;
		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(domain);
		}

		if (d != null) {
			Property property = d.findProperty(propertyName);

			if (property != null) {
				try {
					result = Integer.parseInt(property.getValue());
				} catch (Exception e) {
					//ignore
				}
			}
		}
		return result;
	}

	public int getMaxApiCountThreshold(String domain) {
		return getPropertyValue(domain, "max-api-count-threshold", 100);
	}

	public int getMaxNameThreshold(String domain) {
		return getPropertyValue(domain, "max-name-threshold", 200);
	}

	public int getMaxBusinessItemCount(String domain) {
		return getPropertyValue(domain, "max-business-item-count", 200);
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AtomicMessageConfig messageConfig = DefaultSaxParser.parse(content);

				m_config = messageConfig;
				m_modifyTime = modifyTime;
			}
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

}