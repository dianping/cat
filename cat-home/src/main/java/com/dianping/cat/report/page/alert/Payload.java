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
package com.dianping.cat.report.page.alert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportPage;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("channel")
	private String m_channel;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("startTime")
	private String m_startTime;

	@FieldMeta("endTime")
	private String m_endTime;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("receivers")
	private String m_receivers;

	@FieldMeta("reportType")
	private String m_reportType = "";

	@FieldMeta("alertType")
	private String m_alertType = "";

	@FieldMeta("alertTime")
	private String m_alertTime;

	@FieldMeta("category")
	private String m_category;

	@FieldMeta("level")
	private String m_level;

	@FieldMeta("metric")
	private String m_metric;

	@FieldMeta("count")
	private int m_count;

	private DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public Date getAlertTime() {
		try {
			return m_format.parse(m_alertTime);
		} catch (Exception e) {
			return new Date();
		}
	}

	public void setAlertTime(String alertTime) {
		m_alertTime = alertTime;
	}

	public String getAlertType() {
		return m_alertType;
	}

	public void setAlertType(String alertType) {
		m_alertType = alertType;
	}

	public String[] getAlertTypeArray() {
		List<String> typeList = Splitters.by(",").noEmptyItem().split(m_alertType);
		String[] typeArray = new String[typeList.size()];

		return typeList.toArray(typeArray);
	}

	public String getCategory() {
		if (StringUtils.isEmpty(m_category)) {
			return "zabbix";
		}
		return m_category;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public String getChannel() {
		if (StringUtils.isEmpty(m_channel)) {
			return "";
		} else {
			return m_channel;
		}
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public String getContent() {
		if (StringUtils.isEmpty(m_content)) {
			return "";
		} else {
			return m_content;
		}
	}

	public void setContent(String content) {
		m_content = content;
	}

	public int getCount() {
		if (m_count == 0) {
			return 10;
		} else {
			return m_count;
		}
	}

	public void setCount(int count) {
		m_count = count;
	}

	public String getDomain() {
		if (StringUtils.isEmpty(m_domain)) {
			return null;
		} else {
			return m_domain;
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public Date getEndTime() {
		try {
			return m_format.parse(m_endTime);
		} catch (Exception e) {
			return new Date();
		}
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public String getGroup() {
		if (StringUtils.isEmpty(m_group)) {
			return "default";
		} else {
			return m_group;
		}
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public String getLevel() {
		if (StringUtils.isEmpty(m_level)) {
			return "warning";
		}
		return m_level;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public String getMetric() {
		if (StringUtils.isEmpty(m_metric)) {
			return "";
		} else {
			return m_metric;
		}
	}

	public void setMetric(String metric) {
		m_metric = metric;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ALERT);
	}

	public String getReceivers() {
		if (StringUtils.isEmpty(m_receivers)) {
			return "";
		} else {
			return m_receivers;
		}
	}

	public void setReceivers(String receivers) {
		m_receivers = receivers;
	}

	public String getReportType() {
		return m_reportType;
	}

	public void setReportType(String reportType) {
		m_reportType = "";
	}

	public Date getStartTime() {
		try {
			return m_format.parse(m_startTime);
		} catch (Exception e) {
			return new Date(System.currentTimeMillis() - 15 * TimeHelper.ONE_MINUTE);
		}
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public String getTitle() {
		if (StringUtils.isEmpty(m_title)) {
			return "";
		} else {
			return m_title;
		}
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getType() {
		if (StringUtils.isEmpty(m_type)) {
			return "call";
		} else {
			return m_type;
		}
	}

	public void setType(String type) {
		m_type = type;
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.ALERT;
		}
	}
}
