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
package com.dianping.cat.report.page.dependency;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("all")
	private boolean m_all;

	private ReportPage m_page;

	@FieldMeta("range")
	private int m_range = 24;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("productLine")
	private String productLine;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("hideNav")
	private boolean m_hideNav = true;

	@FieldMeta("tab")
	private String m_tab = "tab1";

	public Payload() {
		super(ReportPage.DEPENDENCY);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LINE_CHART);
	}

	public long getCurrentDate() {
		long timestamp = getCurrentTimeMillis();

		return timestamp - timestamp % TimeHelper.ONE_HOUR;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis() - TimeHelper.ONE_MINUTE * 1;
	}

	public long getDate() {
		long current = getCurrentDate();
		long extra = m_step * TimeHelper.ONE_HOUR;

		if (m_date <= 0) {
			return current + extra;
		} else {
			long result = m_date + extra;

			if (result > current) {
				return current;
			}
			return result;
		}
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
		this.m_minute = minute;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DEPENDENCY);
	}

	public String getProductLine() {
		return productLine;
	}

	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	public int getRange() {
		return m_range;
	}

	public void setRange(int range) {
		m_range = range;
	}

	public String getTab() {
		return m_tab;
	}

	public void setTab(String tab) {
		m_tab = tab;
	}

	public boolean isAll() {
		return m_all;
	}

	public void setAll(boolean all) {
		this.m_all = all;
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public boolean isHideNav() {
		return m_hideNav;
	}

	public void setHideNav(boolean hideNav) {
		m_hideNav = hideNav;
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
			m_action = Action.LINE_CHART;
		}
	}

}
