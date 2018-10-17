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
package com.dianping.cat.report.page.cache;

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

@ModelMeta("cache")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private String m_queryName;

	@EntityMeta
	private CacheReport m_report;

	private String m_pieChart;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HISTORY_REPORT;
	}

	@Override
	public String getDomain() {
		return m_report.getDomain();
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortDomain(m_report.getIps());
		}
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public CacheReport getReport() {
		return m_report;
	}

	public void setReport(CacheReport report) {
		m_report = report;
	}

}
