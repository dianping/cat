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
package com.dianping.cat.report.page.overload;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportPage;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("startTime")
	private String m_startTime;

	@FieldMeta("endTime")
	private String m_endTime;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("showHourly")
	private boolean m_showHourly = true;

	@FieldMeta("showDaily")
	private boolean m_showDaily = true;

	@FieldMeta("showWeekly")
	private boolean m_showWeekly = true;

	@FieldMeta("showMonthly")
	private boolean m_showMonthly = true;

	@FieldMeta("reportType")
	private String m_reportType = "";

	private DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Override
	public Action getAction() {
		return m_action;
	}

	public Date getEndTime() {
		try {
			return m_format.parse(m_endTime);
		} catch (Exception e) {
			return new Date();
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getReportType() {
		return m_reportType;
	}

	public Date getStartTime() {
		try {
			return m_format.parse(m_startTime);
		} catch (Exception e) {
			return new Date(System.currentTimeMillis() - TimeHelper.ONE_DAY);
		}
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public boolean isShowDaily() {
		return m_showDaily;
	}

	public boolean isShowHourly() {
		return m_showHourly;
	}

	public boolean isShowMonthly() {
		return m_showMonthly;
	}

	public boolean isShowWeekly() {
		return m_showWeekly;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.OVERLOAD);
	}

	public void setReportType(String reportType) {
		m_reportType = reportType;
	}

	public void setShowDaily(boolean showDaily) {
		m_showDaily = showDaily;
	}

	public void setShowHourly(boolean showHourly) {
		m_showHourly = showHourly;
	}

	public void setShowMonthly(boolean showMonthly) {
		m_showMonthly = showMonthly;
	}

	public void setShowWeekly(boolean showWeekly) {
		m_showWeekly = showWeekly;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
