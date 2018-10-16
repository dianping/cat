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
package com.dianping.cat.report.page.top;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("count")
	private int m_minuteCounts = 8;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("tops")
	private int m_topCounts = 11;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	public Payload() {
		super(ReportPage.TOP);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	public int getFrequency() {
		return m_frequency;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public String getMinute() {
		return m_minute;
	}

	public void setMinute(String minute) {
		m_minute = minute;
	}

	public int getMinuteCounts() {
		return m_minuteCounts;
	}

	public void setMinuteCounts(int minuteCounts) {
		m_minuteCounts = minuteCounts;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TOP);
	}

	public int getTopCounts() {
		return m_topCounts;
	}

	public void setTopCounts(int topCounts) {
		m_topCounts = topCounts;
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

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
