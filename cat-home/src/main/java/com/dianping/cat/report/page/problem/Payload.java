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
package com.dianping.cat.report.page.problem;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("groupName")
	private String m_groupName;

	@FieldMeta("linkCount")
	private int m_linkCount;

	@FieldMeta("urlThreshold")
	private int m_urlThreshold = 1000;

	@FieldMeta("minute")
	private int m_minute;

	@FieldMeta("sqlThreshold")
	private int m_sqlThreshold = 100;

	@FieldMeta("serviceThreshold")
	private int m_serviceThreshold = 50;

	@FieldMeta("cacheThreshold")
	private int m_cacheThreshold = 10;

	@FieldMeta("callThreshold")
	private int m_callThreshold = 50;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("group")
	private String m_group;

	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOULY_REPORT);
	}

	public int getCacheThreshold() {
		return m_cacheThreshold;
	}

	public void setCacheThreshold(int cacheThreshold) {
		m_cacheThreshold = cacheThreshold;
	}

	public int getCallThreshold() {
		return m_callThreshold;
	}

	public void setCallThreshold(int callThreshold) {
		m_callThreshold = callThreshold;
	}

	public String getGroup() {
		return m_group;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public int getLinkCount() {
		if (m_linkCount < 40) {
			m_linkCount = 40;
		}
		return m_linkCount;
	}

	public void setLinkCount(int linkSize) {
		m_linkCount = linkSize;
	}

	public int getMinute() {
		return m_minute;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public String getQueryString() {
		StringBuilder sb = new StringBuilder();

		sb.append("&urlThreshold=").append(m_urlThreshold);
		sb.append("&sqlThreshold=").append(m_sqlThreshold);
		sb.append("&serviceThreshold=").append(m_serviceThreshold);
		sb.append("&cacheThreshold=").append(m_cacheThreshold);
		sb.append("&callThreshold=").append(m_callThreshold);
		return sb.toString();
	}

	public int getServiceThreshold() {
		return m_serviceThreshold;
	}

	public void setServiceThreshold(int serviceThreshold) {
		m_serviceThreshold = serviceThreshold;
	}

	public int getSqlThreshold() {
		return m_sqlThreshold;
	}

	public void setSqlThreshold(int sqlThreshold) {
		m_sqlThreshold = sqlThreshold;
	}

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public int getUrlThreshold() {
		return m_urlThreshold;
	}

	public void setUrlThreshold(int longTime) {
		m_urlThreshold = longTime;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.PROBLEM);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOULY_REPORT;
		}
	}
}
