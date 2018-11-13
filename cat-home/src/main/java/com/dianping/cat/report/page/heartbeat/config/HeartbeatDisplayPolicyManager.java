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
package com.dianping.cat.report.page.heartbeat.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.home.heartbeat.transform.DefaultSaxParser;

public class HeartbeatDisplayPolicyManager implements Initializable {

	private static final int K = 1024;

	private static final String CONFIG_NAME = "heartbeat-display-policy";

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private HeartbeatDisplayPolicy m_config;

	public HeartbeatDisplayPolicy getHeartbeatDisplayPolicy() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
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
			m_config = new HeartbeatDisplayPolicy();
		}
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

	public boolean isDelta(String groupName, String metricName) {
		Group group = m_config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric.isDelta();
			}
		}
		return false;
	}

	public Metric queryMetric(String groupName, String metricName) {
		Group group = m_config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric;
			}
		}
		return null;
	}

	public List<String> queryAlertMetrics() {
		List<String> metrics = new ArrayList<String>();

		for (Group group : m_config.getGroups().values()) {
			String groupId = group.getId();

			for (Metric metric : group.getMetrics().values()) {
				if (metric.isAlert()) {
					metrics.add(groupId + ":" + metric.getId());
				}
			}
		}
		return metrics;
	}

	public int queryUnit(String groupName, String metricName) {
		Group group = m_config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				String metricUnit = metric.getUnit();

				if ("K".equals(metricUnit)) {
					return K;
				} else if ("M".equals(metricUnit)) {
					return K * K;
				} else if ("G".equals(metricUnit)) {
					return K * K * K;
				} else {
					return Integer.parseInt(metricUnit);
				}
			}
		}
		return 1;
	}

	public List<String> sortGroupNames(List<String> originGroupNames) {
		List<Group> groups = new ArrayList<Group>();

		for (Entry<String, Group> entry : m_config.getGroups().entrySet()) {
			if (originGroupNames.contains(entry.getKey())) {
				groups.add(entry.getValue());
			}
		}
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group g1, Group g2) {
				return g1.getOrder() - g2.getOrder();
			}
		});

		List<String> result = new ArrayList<String>();

		for (Group group : groups) {
			result.add(group.getId());
		}
		for (String originGroupName : originGroupNames) {
			if (!result.contains(originGroupName)) {
				result.add(originGroupName);
			}
		}
		return result;
	}

	public List<String> sortGroupNames(Set<String> originGroupNameSet) {
		return sortGroupNames(new ArrayList<String>(originGroupNameSet));
	}

	public List<String> sortMetricNames(String groupName, List<String> originMetricNames) {
		Group group = m_config.findGroup(groupName);
		List<String> result = new ArrayList<String>();

		if (group != null) {
			List<Metric> list = new ArrayList<Metric>();

			for (Entry<String, Metric> entry : group.getMetrics().entrySet()) {
				if (originMetricNames.contains(entry.getKey())) {
					list.add(entry.getValue());
				}
			}
			Collections.sort(list, new Comparator<Metric>() {
				@Override
				public int compare(Metric m1, Metric m2) {
					return m1.getOrder() - m2.getOrder();
				}
			});
			for (Metric metric : list) {
				result.add(metric.getId());
			}
		}

		for (String originMetricName : originMetricNames) {
			if (!result.contains(originMetricName)) {
				result.add(originMetricName);
			}
		}
		return result;
	}

	public List<String> sortMetricNames(String groupName, Set<String> originMetricNames) {
		return sortMetricNames(groupName, new ArrayList<String>(originMetricNames));
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
