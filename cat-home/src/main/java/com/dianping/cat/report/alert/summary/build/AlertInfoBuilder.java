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
package com.dianping.cat.report.alert.summary.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.Alert;
import com.dianping.cat.alarm.AlertDao;
import com.dianping.cat.alarm.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

@Named
public class AlertInfoBuilder {

	public static final String LONG_CALL = "long_call";

	public static final String PREFIX = "dependency_";

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private TopologyGraphManager m_topologyManager;

	private Collection<com.dianping.cat.home.alert.summary.entity.Alert> convertToAlert(List<TopologyEdge> edges,
							Date date) {
		Map<String, com.dianping.cat.home.alert.summary.entity.Alert> alerts = new LinkedHashMap<String, com.dianping.cat.home.alert.summary.entity.Alert>();

		for (TopologyEdge edge : edges) {
			String domain = edge.getSelf();
			String metric = edge.getKey();
			String key = domain + ":" + metric;
			com.dianping.cat.home.alert.summary.entity.Alert alertInMap = alerts.get(key);

			if (alertInMap == null) {
				com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();
				alert.setAlertTime(date);
				alert.setContext(edge.getDes());
				alert.setMetric(metric);
				alert.setType("slow " + edge.getType());
				alert.setDomain(domain);
				alert.setCount(1);

				alerts.put(key, alert);
			} else {
				int originCount = alertInMap.getCount();
				alertInMap.setCount(originCount + 1);
			}
		}

		return alerts.values();
	}

	private Collection<com.dianping.cat.home.alert.summary.entity.Alert> convertToAlerts(List<Alert> dbAlerts) {
		Map<String, com.dianping.cat.home.alert.summary.entity.Alert> alerts = new LinkedHashMap<String, com.dianping.cat.home.alert.summary.entity.Alert>();

		for (Alert dbAlert : dbAlerts) {
			String domain = dbAlert.getDomain();
			String metric = dbAlert.getMetric();
			String key = domain + ":" + metric;
			com.dianping.cat.home.alert.summary.entity.Alert alertInMap = alerts.get(key);

			if (alertInMap == null) {
				com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();
				alert.setAlertTime(dbAlert.getAlertTime());
				alert.setContext(dbAlert.getContent());
				alert.setMetric(metric);
				alert.setType(dbAlert.getType());
				alert.setDomain(domain);
				alert.setCount(1);

				alerts.put(key, alert);
			} else {
				int originCount = alertInMap.getCount();
				alertInMap.setCount(originCount + 1);
			}
		}

		return alerts.values();
	}

	public AlertSummary generateAlertSummary(String domain, Date date) {
		AlertSummary alertSummary = new AlertSummary();

		alertSummary.setDomain(domain);
		alertSummary.setAlertDate(date);

		alertSummary.addCategory(generateCategoryByTimeCateDomain(date, AlertType.Business.getName(), domain));
		alertSummary.addCategory(generateCategoryByTimeCateDomain(date, AlertType.Exception.getName(), domain));

		TopologyGraph topology = m_topologyManager.buildTopologyGraph(domain, date.getTime());
		int statusThreshold = 2;

		alertSummary.addCategory(generateLongCallCategory(date, topology, statusThreshold));

		List<String> dependencyDomains = queryDependencyDomains(topology, date, domain);
		alertSummary
								.addCategory(generateDependCategoryByTimeCateDomain(date, AlertType.Exception.getName(), dependencyDomains));

		return alertSummary;
	}

	private Category generateCategoryByTimeCateDomain(Date date, String cate, String domain) {
		Category category = new Category(cate);
		String dbCategoryName = cate;
		Date startTime = new Date(date.getTime() - AlertSummaryExecutor.SUMMARY_DURATION);

		try {
			List<Alert> dbAlerts = m_alertDao
									.queryAlertsByTimeCategoryDomain(startTime, date, dbCategoryName, domain, AlertEntity.READSET_FULL);
			setDBAlertsToCategory(category, dbAlerts);
		} catch (DalException e) {
			Cat.logError("find alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
		}

		return category;
	}

	private Category generateDependCategoryByTimeCateDomain(Date date, String cate, List<String> dependencyDomains) {
		String categoryName = PREFIX + cate;
		String dbCategoryName = cate;
		Category category = new Category(categoryName);
		Date startTime = new Date(date.getTime() - AlertSummaryExecutor.SUMMARY_DURATION);

		for (String domain : dependencyDomains) {
			try {
				List<Alert> dbAlerts = m_alertDao
										.queryAlertsByTimeCategoryDomain(startTime, date, dbCategoryName, domain, AlertEntity.READSET_FULL);

				setDBAlertsToCategory(category, dbAlerts);
			} catch (DalException e) {
				Cat.logError("find dependency alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
			}
		}

		return category;
	}

	private Category generateLongCallCategory(Date date, TopologyGraph topology, int statusThreshold) {
		Category category = new Category(LONG_CALL);
		List<TopologyEdge> edges = new ArrayList<TopologyEdge>();

		for (TopologyEdge edge : topology.getEdges().values()) {
			if (edge.getStatus() >= statusThreshold) {
				edges.add(edge);
			}
		}

		Collection<com.dianping.cat.home.alert.summary.entity.Alert> alerts = convertToAlert(edges, date);
		Iterator<com.dianping.cat.home.alert.summary.entity.Alert> it = alerts.iterator();
		while (it.hasNext()) {
			category.addAlert(it.next());
		}

		return category;
	}

	private List<String> queryDependencyDomains(TopologyGraph topology, Date date, String domain) {
		List<String> domains = new ArrayList<String>();

		for (String dependencyDomain : topology.getNodes().keySet()) {
			domains.add(dependencyDomain);
		}

		return domains;
	}

	private void setDBAlertsToCategory(Category category, List<Alert> dbAlerts) {
		Collection<com.dianping.cat.home.alert.summary.entity.Alert> alerts = convertToAlerts(dbAlerts);
		Iterator<com.dianping.cat.home.alert.summary.entity.Alert> it = alerts.iterator();

		while (it.hasNext()) {
			category.addAlert(it.next());
		}
	}
}
