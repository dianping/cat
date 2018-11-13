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
package com.dianping.cat.report.page.cross;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private static int HOUR = 60 * 60 * 1000;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("callSort")
	private String m_callSort = "avg";

	private ReportPage m_page;

	@FieldMeta("project")
	private String m_projectName = "All";

	@FieldMeta("remote")
	private String m_remoteIp;

	@FieldMeta("serviceSort")
	private String m_serviceSort = "avg";

	@FieldMeta("queryName")
	private String m_queryName;

	@FieldMeta("method")
	private String m_method;

	private String m_rawDate;

	public Payload() {
		super(ReportPage.CROSS);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_PROJECT);
	}

	public String getCallSort() {
		return m_callSort;
	}

	public void setCallSort(String callSort) {
		m_callSort = callSort;
	}

	public long getHourDuration() {
		long duration = HOUR / 1000;
		if (getPeriod().isCurrent()) {
			duration = System.currentTimeMillis() % HOUR / 1000;
		}
		return duration;
	}

	public String getMethod() {
		return m_method;
	}

	public void setMethod(String method) {
		m_method = method;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.CROSS);
	}

	public String getProjectName() {
		return m_projectName;
	}

	public void setProjectName(String projectName) {
		m_projectName = projectName;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public String getRawDate() {
		return m_rawDate;
	}

	public String getRemoteIp() {
		return m_remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		m_remoteIp = remoteIp;
	}

	public String getServiceSort() {
		return m_serviceSort;
	}

	public void setServiceSort(String serviceSort) {
		m_serviceSort = serviceSort;
	}

	public void setDate(String date) {
		m_rawDate = date;

		super.setDate(date);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_PROJECT;
		}
	}

}
