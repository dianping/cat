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
package com.dianping.cat.report.page.transaction;

import java.net.URLEncoder;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("queryname")
	private String m_queryName;

	@FieldMeta("sort")
	private String m_sortBy;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("xml")
	private boolean m_xml;

	@FieldMeta("group")
	private String m_group;

	public Payload() {
		super(ReportPage.TRANSACTION);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	public String getEncodedType() {
		try {
			return URLEncoder.encode(m_type, "utf-8");
		} catch (Exception e) {
			return m_type;
		}
	}

	public String getGroup() {
		return m_group;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		this.m_queryName = queryName;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public boolean isXml() {
		return m_xml;
	}

	public void setXml(boolean xml) {
		m_xml = xml;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TRANSACTION);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
	}

}
