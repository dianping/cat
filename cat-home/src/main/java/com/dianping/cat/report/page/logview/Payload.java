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
package com.dianping.cat.report.page.logview;

import java.util.Arrays;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("op")
	private Action m_action = Action.VIEW;

	@PathMeta("path")
	private String[] m_path;

	@FieldMeta("header")
	private boolean m_showHeader = true;

	@FieldMeta("waterfall")
	private boolean m_waterfall = false;

	public Payload() {
		super(ReportPage.LOGVIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, m_action);
	}

	public String[] getPath() {
		return m_path;
	}

	public void setPath(String[] path) {
		if (path == null) {
			m_path = new String[0];
		} else {
			m_path = Arrays.copyOf(path, path.length);
		}
	}

	public boolean isShowHeader() {
		return m_showHeader;
	}

	public void setShowHeader(String showHeader) {
		m_showHeader = !"no".equals(showHeader);
	}

	public boolean isWaterfall() {
		return m_waterfall;
	}

	public void setWaterfall(boolean waterfall) {
		m_waterfall = waterfall;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.LOGVIEW);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
