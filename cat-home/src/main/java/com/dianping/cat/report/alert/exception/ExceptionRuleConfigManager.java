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
package com.dianping.cat.report.alert.exception;

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
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;
import com.dianping.cat.home.exception.transform.DefaultSaxParser;

@Named
public class ExceptionRuleConfigManager implements Initializable {

	private static final String CONFIG_NAME = "exceptionRuleConfig";

	public static String DEFAULT_STRING = "Default";

	public static String TOTAL_STRING = "Total";

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private ExceptionRuleConfig m_exceptionRuleConfig;

	public boolean deleteExceptionExclude(String domain, String exceptionName) {
		m_exceptionRuleConfig.removeExceptionExclude(domain + ":" + exceptionName);

		return storeConfig();
	}

	public boolean deleteExceptionLimit(String domain, String exceptionName) {
		m_exceptionRuleConfig.removeExceptionLimit(domain + ":" + exceptionName);

		return storeConfig();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			m_configId = config.getId();
			m_exceptionRuleConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_exceptionRuleConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_exceptionRuleConfig == null) {
			m_exceptionRuleConfig = new ExceptionRuleConfig();
		}
	}

	public boolean isExcluded(String domain, String exceptionName) {
		boolean excluded = false;
		ExceptionExclude result = queryExceptionExclude(domain, exceptionName);

		if (result != null) {
			excluded = true;
		}
		return excluded;
	}

	public boolean insertExceptionExclude(ExceptionExclude exclude) {
		String id = exclude.getDomain() + ":" + exclude.getName();

		m_exceptionRuleConfig.getExceptionExcludes().put(id, exclude);
		return storeConfig();
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		String id = limit.getDomain() + ":" + limit.getName();

		m_exceptionRuleConfig.getExceptionLimits().put(id, limit);
		return storeConfig();
	}

	public List<ExceptionExclude> queryAllExceptionExcludes() {
		return new ArrayList<ExceptionExclude>(m_exceptionRuleConfig.getExceptionExcludes().values());
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		return new ArrayList<ExceptionLimit>(m_exceptionRuleConfig.getExceptionLimits().values());
	}

	public ExceptionExclude queryExceptionExclude(String domain, String exceptionName) {
		ExceptionExclude exceptionExclude = m_exceptionRuleConfig.findExceptionExclude(domain + ":" + exceptionName);

		if (exceptionExclude == null) {
			exceptionExclude = m_exceptionRuleConfig.findExceptionExclude(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionExclude;
	}

	public ExceptionLimit queryExceptionLimit(String domain, String exceptionName) {
		ExceptionLimit exceptionLimit = m_exceptionRuleConfig.findExceptionLimit(domain + ":" + exceptionName);

		if (exceptionLimit == null) {
			exceptionLimit = m_exceptionRuleConfig.findExceptionLimit(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionLimit;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_exceptionRuleConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public ExceptionLimit queryTotalLimitByDomain(String domain) {
		return queryExceptionLimit(domain, TOTAL_STRING);
	}

}
