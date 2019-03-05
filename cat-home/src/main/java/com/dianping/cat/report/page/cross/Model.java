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

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;

@ModelMeta(CrossAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private String m_callSort;

	private String m_queryName;

	@EntityMeta
	private HostInfo m_hostInfo;

	@EntityMeta
	private MethodInfo m_methodInfo;

	@EntityMeta
	private ProjectInfo m_projectInfo;

	@EntityMeta
	private CrossReport m_report;

	private String m_serviceSort;

	@EntityMeta
	private MethodQueryInfo m_info;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getCallSort() {
		return m_callSort;
	}

	public void setCallSort(String callSort) {
		m_callSort = callSort;
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_PROJECT;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	public HostInfo getHostInfo() {
		return m_hostInfo;
	}

	public void setHostInfo(HostInfo hostInfo) {
		m_hostInfo = hostInfo;
	}

	public MethodQueryInfo getInfo() {
		return m_info;
	}

	public void setInfo(MethodQueryInfo info) {
		m_info = info;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public MethodInfo getMethodInfo() {
		return m_methodInfo;
	}

	public void setMethodInfo(MethodInfo methodInfo) {
		m_methodInfo = methodInfo;
	}

	public ProjectInfo getProjectInfo() {
		return m_projectInfo;
	}

	public void setProjectInfo(ProjectInfo projectInfo) {
		m_projectInfo = projectInfo;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public CrossReport getReport() {
		return m_report;
	}

	public void setReport(CrossReport report) {
		m_report = report;
	}

	public String getServiceSort() {
		return m_serviceSort;
	}

	public void setServiceSort(String serviceSort) {
		m_serviceSort = serviceSort;
	}

}
