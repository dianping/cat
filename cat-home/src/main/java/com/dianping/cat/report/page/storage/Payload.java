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
package com.dianping.cat.report.page.storage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type = StorageSQLBuilder.ID;

	@FieldMeta("operations")
	private String m_operations;

	@FieldMeta("project")
	private String m_project;

	@FieldMeta("sort")
	private String m_sort = "domain";

	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("count")
	private int m_minuteCounts = 8;

	@FieldMeta("id")
	private String m_id = Constants.CAT;

	public Payload() {
		super(ReportPage.STORAGE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_STORAGE);
	}

	@Override
	public long getCurrentDate() {
		long timestamp = getCurrentTimeMillis();

		return timestamp - timestamp % TimeHelper.ONE_HOUR;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis() - TimeHelper.ONE_MINUTE * 1;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
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

	public String getOperations() {
		return m_operations;
	}

	public void setOperations(String operations) {
		m_operations = operations;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STORAGE);
	}

	public String getProject() {
		return m_project;
	}

	public void setProject(String project) {
		m_project = project;
	}

	public String getSort() {
		return m_sort;
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	public String getType() {
		return m_type;
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
			m_action = Action.HOURLY_STORAGE;
		}
	}
}
