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
package com.dianping.cat.report.alert.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;
import com.dianping.cat.alarm.rule.transform.DefaultJsonParser;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class BusinessRuleConfigManager implements Initializable {

	private static final String ALERT_CONFIG = "alert";

	private static final String TYPE = "type";

	private static final String SPLITTER = ":";

	Map<String, MonitorRules> m_rules = new ConcurrentHashMap<String, MonitorRules>();

	@Inject
	private BusinessConfigDao m_configDao;

	private List<Config> buildDefaultConfigs() {
		List<Config> configs = new ArrayList<Config>();
		Config config = new Config();
		config.setStarttime("00:00");
		config.setEndtime("24:00");

		Condition condition = new Condition();
		SubCondition descPerSubcon = new SubCondition();
		SubCondition descValSubcon = new SubCondition();
		SubCondition flucPerSubcon = new SubCondition();

		descPerSubcon.setType("DescPer").setText("50");
		descValSubcon.setType("DescVal").setText("100");
		flucPerSubcon.setType("FluDescPer").setText("20");
		condition.addSubCondition(descPerSubcon).addSubCondition(descValSubcon).addSubCondition(flucPerSubcon);
		config.addCondition(condition);

		configs.add(config);

		return configs;
	}

	private String generateRuleId(String key, String type) {
		return new StringBuilder().append(key).append(SPLITTER).append(type).toString();
	}

	public Map<MetricType, List<Config>> getDefaultRules(BusinessItemConfig config) {
		Map<MetricType, List<Config>> configs = new HashMap<MetricType, List<Config>>();

		if (config.isShowAvg()) {
			configs.put(MetricType.AVG, buildDefaultConfigs());
		}

		if (config.isShowCount()) {
			configs.put(MetricType.COUNT, buildDefaultConfigs());
		}

		if (config.isShowSum()) {
			configs.put(MetricType.SUM, buildDefaultConfigs());
		}
		return configs;
	}

	public Map<MetricType, List<Config>> getDefaultRulesForCustomItem() {
		Map<MetricType, List<Config>> configs = new HashMap<MetricType, List<Config>>();

		configs.put(MetricType.AVG, buildDefaultConfigs());

		return configs;
	}

	@Override
	public void initialize() throws InitializationException {
		loadData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return ALERT_CONFIG;
			}

			@Override
			public void handle() throws Exception {
				loadData();
			}
		});
	}

	private void loadData() {
		try {
			List<BusinessConfig> configs = m_configDao.findByName(ALERT_CONFIG, BusinessConfigEntity.READSET_FULL);
			Map<String, MonitorRules> rules = new ConcurrentHashMap<String, MonitorRules>();

			for (BusinessConfig config : configs) {
				try {
					String doamin = config.getDomain();
					MonitorRules rule = DefaultSaxParser.parse(config.getContent());
					rules.put(doamin, rule);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
			m_rules = rules;
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public List<Config> queryConfigs(String domain, String key, MetricType type) {
		String typeName = type.getName();
		Rule rule = queryRule(domain, key, typeName);
		List<Config> configs = new ArrayList<Config>();

		if (rule != null && rule.getDynamicAttribute(TYPE).equals(typeName)) {
			configs.addAll(rule.getConfigs());
		}

		return configs;
	}

	public MonitorRules queryMonitorRules(String domain) {
		return m_rules.get(domain);
	}

	public Rule queryRule(String domain, String key, String type) {
		MonitorRules rule = m_rules.get(domain);

		if (rule != null) {
			return rule.findRule(generateRuleId(key, type));
		} else {
			return null;
		}
	}

	public void updateRule(String domain, String key, String configsStr, String type) {
		try {
			Rule rule = new Rule(generateRuleId(key, type));
			List<Config> configs = DefaultJsonParser.parseArray(Config.class, configsStr);

			for (Config config : configs) {
				rule.addConfig(config);
			}

			rule.setDynamicAttribute(TYPE, type);

			boolean isExist = true;
			MonitorRules domainRule = m_rules.get(domain);

			if (domainRule == null) {
				domainRule = new MonitorRules();
				m_rules.put(domain, domainRule);
				isExist = false;
			}

			domainRule.getRules().put(rule.getId(), rule);

			BusinessConfig proto = m_configDao.createLocal();
			proto.setDomain(domain);
			proto.setContent(domainRule.toString());
			proto.setName(ALERT_CONFIG);
			proto.setUpdatetime(new Date());

			if (isExist) {
				m_configDao.updateBaseConfigByDomain(proto, BusinessConfigEntity.UPDATESET_FULL);
			} else {
				m_configDao.insert(proto);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

	}
}
