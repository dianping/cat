/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dianping.cat.config.web.js;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.web.js.entity.Aggregation;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.configuration.web.js.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AggregationConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected AggregationHandler m_handler;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private static final String CONFIG_NAME = "aggreationConfig";

	private volatile Aggregation m_aggregation;

	private long m_modifyTime;

	public static final int PROBLEM_TYPE = 3;

	public boolean deleteAggregationRule(String rule) {
		m_aggregation.removeAggregationRule(rule);
		m_handler.register(queryAggregationRules());
		return storeConfig();
	}

	public String handle(int type, String domain, String status) {
		return m_handler.handle(type, domain, status);
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_aggregation = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_aggregation = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_aggregation == null) {
			m_aggregation = new Aggregation();
		}
		m_handler.register(queryAggregationRules());

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

			@Override
			public String getName() {
				return CONFIG_NAME;
			}
		});	}

	public boolean insertAggregationRule(AggregationRule rule) {
		m_aggregation.addAggregationRule(rule);
		m_handler.register(queryAggregationRules());
		return storeConfig();
	}

	public List<AggregationRule> queryAggrarationRulesFromDB() {
		try {
			m_aggregation = queryAggreation();

			return new ArrayList<AggregationRule>(m_aggregation.getAggregationRules().values());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new ArrayList<AggregationRule>();
	}

	public AggregationRule queryAggration(String key) {
		return m_aggregation.findAggregationRule(key);
	}

	private Aggregation queryAggreation() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			return DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Aggregation();
	}

	public List<AggregationRule> queryAggregationRules() {
		return new ArrayList<AggregationRule>(m_aggregation.getAggregationRules().values());
	}

	private void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				Aggregation aggregation = DefaultSaxParser.parse(content);

				m_aggregation = aggregation;
				m_handler.register(queryAggregationRules());
				m_modifyTime = modifyTime;
			}
		}
	}

	public void refreshRule() {
		List<AggregationRule> rules = queryAggrarationRulesFromDB();

		m_handler.register(rules);
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_aggregation.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

}
