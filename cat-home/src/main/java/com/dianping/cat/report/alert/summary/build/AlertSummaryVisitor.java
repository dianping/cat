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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;
import com.dianping.cat.home.alert.summary.transform.BaseVisitor;

public class AlertSummaryVisitor extends BaseVisitor {

	public static final String LONG_CALL_NAME = "超时依赖调用";

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private Map<Object, Object> m_categoryMap = new LinkedHashMap<Object, Object>();

	private List<Map<Object, Object>> m_alertList;

	private DateFormat m_fmt = new SimpleDateFormat("HH:mm");

	private String m_domain;

	public AlertSummaryVisitor(String domain) {
		m_domain = domain;
	}

	private String convertNameToChinese(String name) {
		if (name.equals(AlertType.Business.getName())) {
			return "业务告警";
		}
		if (name.equals(AlertType.Exception.getName())) {
			return "异常告警";
		}
		if (name.equals(AlertInfoBuilder.LONG_CALL)) {
			return LONG_CALL_NAME;
		}
		if (name.equals(AlertInfoBuilder.PREFIX + AlertType.Exception.getName())) {
			return "依赖异常告警";
		}

		return "";
	}

	public Map<Object, Object> getResult() {
		return m_result;
	}

	@Override
	public void visitAlert(Alert alert) {
		Map<Object, Object> tmpAlertMap = new HashMap<Object, Object>();

		String alertDomain = alert.getDomain();
		if (alertDomain != null && alertDomain.equals(m_domain)) {
			tmpAlertMap.put("metric", alert.getMetric());
		} else {
			tmpAlertMap.put("metric", alertDomain + "<br>" + alert.getMetric());
		}
		tmpAlertMap.put("domain", alert.getDomain());
		tmpAlertMap.put("dateStr", m_fmt.format(alert.getAlertTime()));
		tmpAlertMap.put("type", alert.getType());
		tmpAlertMap.put("context", alert.getContext());
		tmpAlertMap.put("count", alert.getCount());

		m_alertList.add(tmpAlertMap);
	}

	@Override
	public void visitAlertSummary(AlertSummary alertSummary) {
		Date date = alertSummary.getAlertDate();
		m_result.put("domain", alertSummary.getDomain());
		m_result.put("dateStr", m_fmt.format(date));
		m_result.put("categories", m_categoryMap);

		for (Category category : alertSummary.getCategories().values()) {
			visitCategory(category);
		}
	}

	@Override
	public void visitCategory(Category category) {
		m_alertList = new ArrayList<Map<Object, Object>>();

		for (Alert alert : category.getAlerts()) {
			visitAlert(alert);
		}

		m_categoryMap.put(convertNameToChinese(category.getName()), m_alertList);
	}
}
