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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.report.alert.summary.AlertSummaryService;

@Named(type = SummaryBuilder.class, value = RelatedSummaryBuilder.ID)
public class RelatedSummaryBuilder extends SummaryBuilder {

	public static final String ID = "AlertSummaryContentGenerator";

	@Inject
	private AlertInfoBuilder m_alertSummaryManager;

	@Inject
	private AlertSummaryService m_alertSummaryService;

	@SuppressWarnings("unchecked")
	private Map<Object, Object> gatherDomainsForDependBusiness(Map<Object, Object> map) {
		try {
			Map<Object, Object> categories = (Map<Object, Object>) map.get("categories");
			List<Map<Object, Object>> alerts = (List<Map<Object, Object>>) categories.get(AlertSummaryVisitor.LONG_CALL_NAME);
			Map<String, List<Map<Object, Object>>> longCallMap = new TreeMap<String, List<Map<Object, Object>>>();

			for (Map<Object, Object> alert : alerts) {
				String domain = (String) alert.get("domain");
				List<Map<Object, Object>> tmpAlerts = longCallMap.get(domain);

				if (tmpAlerts == null) {
					tmpAlerts = new ArrayList<Map<Object, Object>>();
					longCallMap.put(domain, tmpAlerts);
				}
				tmpAlerts.add(alert);
			}

			categories.remove(AlertSummaryVisitor.LONG_CALL_NAME);
			categories.put(AlertInfoBuilder.LONG_CALL, longCallMap);
			map.put(AlertInfoBuilder.LONG_CALL + "_length", alerts.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return map;
	}

	@Override
	public Map<Object, Object> generateModel(String domain, Date date) {
		AlertSummary alertSummary = m_alertSummaryManager.generateAlertSummary(domain, date);
		AlertSummaryVisitor visitor = new AlertSummaryVisitor(alertSummary.getDomain());

		visitor.visitAlertSummary(alertSummary);

		return gatherDomainsForDependBusiness(visitor.getResult());
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected String getTemplateAddress() {
		return "summary.ftl";
	}

}
