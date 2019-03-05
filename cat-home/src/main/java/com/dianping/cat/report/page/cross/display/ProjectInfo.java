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
package com.dianping.cat.report.page.cross.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;

public class ProjectInfo extends BaseVisitor {

	public static final String ALL_SERVER = "AllServers";

	public static final String ALL_CLIENT = "AllClients";

	private Map<String, TypeDetailInfo> m_callProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_serviceProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_callerProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private String m_clientIp;

	private long m_reportDuration;

	private String m_callSortBy = "Avg";

	private String m_serviceSortBy = "Avg";

	public ProjectInfo(long reportDuration) {
		m_reportDuration = reportDuration;
	}

	private void addCallerProject(String ip, String app, Type type) {
		String projectName = app;
		TypeDetailInfo all = m_callerProjectsInfo.get(ALL_CLIENT);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			m_callerProjectsInfo.put(ALL_CLIENT, all);
		}

		TypeDetailInfo info = m_callerProjectsInfo.get(projectName);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_callerProjectsInfo.put(projectName, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	public void addCallerProjectInfo(String domain, TypeDetailInfo info) {
		TypeDetailInfo all = m_callerProjectsInfo.get(ALL_CLIENT);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			all.setType(info.getType());
			m_callerProjectsInfo.put(ALL_CLIENT, all);
		}
		all.mergeTypeDetailInfo(info);
		m_callerProjectsInfo.put(domain, info);
	}

	private void addCallProject(String ip, String app, Type type) {
		String projectName = app;
		TypeDetailInfo all = m_callProjectsInfo.get(ALL_SERVER);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_SERVER);
			m_callProjectsInfo.put(ALL_SERVER, all);
		}
		TypeDetailInfo info = m_callProjectsInfo.get(projectName);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_callProjectsInfo.put(projectName, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	private void addServiceProject(String ip, String app, Type type) {
		String projectName = app;
		TypeDetailInfo all = m_serviceProjectsInfo.get(ALL_CLIENT);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			m_serviceProjectsInfo.put(ALL_CLIENT, all);
		}
		TypeDetailInfo info = m_serviceProjectsInfo.get(projectName);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_serviceProjectsInfo.put(projectName, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	public Map<String, TypeDetailInfo> getAllCallProjectInfo() {
		return m_callProjectsInfo;
	}

	public Map<String, TypeDetailInfo> getCallerProjectsInfo() {
		return m_callerProjectsInfo;
	}

	public Collection<TypeDetailInfo> getCallProjectsInfo() {
		List<TypeDetailInfo> values = new ArrayList<TypeDetailInfo>(m_callProjectsInfo.values());
		Collections.sort(values, new TypeComparator(m_callSortBy));
		return values;
	}

	public long getReportDuration() {
		return m_reportDuration;
	}

	public List<TypeDetailInfo> getServiceProjectsInfo() {
		List<TypeDetailInfo> values = new ArrayList<TypeDetailInfo>(m_serviceProjectsInfo.values());
		Collections.sort(values, new TypeComparator(m_serviceSortBy));
		return values;
	}

	public ProjectInfo setCallSortBy(String callSoryBy) {
		m_callSortBy = callSoryBy;
		return this;
	}

	public ProjectInfo setClientIp(String clientIp) {
		m_clientIp = clientIp;
		return this;
	}

	public ProjectInfo setServiceSortBy(String serviceSortBy) {
		m_serviceSortBy = serviceSortBy;
		return this;
	}

	@Override
	public void visitCrossReport(CrossReport crossReport) {
		super.visitCrossReport(crossReport);
	}

	@Override
	public void visitLocal(Local local) {
		if (m_clientIp.equals("All") || m_clientIp.equals(local.getId())) {
			super.visitLocal(local);
		}
	}

	@Override
	public void visitRemote(Remote remote) {
		String remoteIp = remote.getIp();

		if (remoteIp == null) {
			remoteIp = remote.getId();
		}
		String role = remote.getRole();
		String app = remote.getApp();

		if (role != null && role.endsWith("Client")) {
			addServiceProject(remoteIp, app, remote.getType());
		} else if (role != null && role.endsWith("Server")) {
			addCallProject(remoteIp, app, remote.getType());
		} else if (role != null && role.endsWith("Caller")) {
			addCallerProject(remoteIp, app, remote.getType());
		}
	}

}
