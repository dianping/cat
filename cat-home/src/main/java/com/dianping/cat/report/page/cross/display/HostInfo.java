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

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;
import com.dianping.cat.service.HostinfoService;

public class HostInfo extends BaseVisitor {

	public static final String ALL_CLIENT_IP = "AllClientIP";

	public static final String ALL_SERVER_IP = "AllServerIP";

	private Map<String, TypeDetailInfo> m_callProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_serviceProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_callerProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private String m_callSortBy = "Avg";

	private String m_clientIp;

	private String m_projectName;

	private long m_reportDuration;

	private String m_serviceSortBy = "Avg";

	private HostinfoService m_hostinfoService;

	public HostInfo(long reportDuration) {
		m_reportDuration = reportDuration;
	}

	private void addCallerProject(String ip, Type type) {
		TypeDetailInfo all = m_callerProjectsInfo.get(ALL_CLIENT_IP);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration);
			all.setIp(ALL_CLIENT_IP);
			m_callerProjectsInfo.put(ALL_CLIENT_IP, all);
		}
		TypeDetailInfo info = m_callerProjectsInfo.get(ip);

		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration);
			info.setIp(ip);
			m_callerProjectsInfo.put(ip, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	private void addCallProject(String ip, Type type) {
		TypeDetailInfo all = m_callProjectsInfo.get(ALL_SERVER_IP);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration);
			all.setIp(ALL_SERVER_IP);
			m_callProjectsInfo.put(ALL_SERVER_IP, all);
		}
		TypeDetailInfo info = m_callProjectsInfo.get(ip);

		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration);
			info.setIp(ip);
			m_callProjectsInfo.put(ip, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	private void addServiceProject(String ip, Type type) {
		TypeDetailInfo all = m_serviceProjectsInfo.get(ALL_CLIENT_IP);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration);
			all.setIp(ALL_CLIENT_IP);
			m_serviceProjectsInfo.put(ALL_CLIENT_IP, all);
		}
		TypeDetailInfo info = m_serviceProjectsInfo.get(ip);

		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration);
			info.setIp(ip);
			m_serviceProjectsInfo.put(ip, info);
		}
		info.mergeType(type);
		all.mergeType(type);
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

	public boolean projectContains(String ip, String app, String projectName, String role) {
		if (role.endsWith("Server")) {
			if (ProjectInfo.ALL_SERVER.equals(projectName)) {
				return true;
			}
		} else if (role.endsWith("Client") || role.endsWith("Caller")) {
			if (ProjectInfo.ALL_CLIENT.equals(projectName)) {
				return true;
			}
		}

		if (StringUtils.isNotEmpty(app)) {
			if (app.equalsIgnoreCase(projectName)) {
				return true;
			}
		} else {
			if (ip.indexOf(':') > 0) {
				ip = ip.substring(0, ip.indexOf(':'));
			}
			String domain = m_hostinfoService.queryDomainByIp(ip);
			if (projectName.equalsIgnoreCase(domain)) {
				return true;
			}
		}
		return false;
	}

	public HostInfo setCallSortBy(String callSoryBy) {
		m_callSortBy = callSoryBy;
		return this;
	}

	public HostInfo setClientIp(String clientIp) {
		m_clientIp = clientIp;
		return this;
	}

	public void setHostinfoService(HostinfoService hostinfoService) {
		m_hostinfoService = hostinfoService;
	}

	public HostInfo setProjectName(String projectName) {
		this.m_projectName = projectName;
		return this;
	}

	public HostInfo setServiceSortBy(String serviceSortBy) {
		m_serviceSortBy = serviceSortBy;
		return this;
	}

	@Override
	public void visitCrossReport(CrossReport crossReport) {
		super.visitCrossReport(crossReport);
	}

	@Override
	public void visitLocal(Local local) {
		if (m_clientIp.equalsIgnoreCase("All") || m_clientIp.equalsIgnoreCase(local.getId())) {
			super.visitLocal(local);
		}
	}

	@Override
	public void visitRemote(Remote remote) {
		String id = remote.getId();
		String remoteIp = remote.getIp();

		if (remoteIp == null) {
			remoteIp = id;

			int index = remoteIp.indexOf(":Caller");

			if (index > -1) {
				remoteIp = remoteIp.substring(0, index);
			}
		}

		String role = remote.getRole();
		String app = remote.getApp();
		boolean flag = projectContains(id, app, m_projectName, role);

		if (flag) {
			if (role != null && role.endsWith("Client")) {
				addServiceProject(remoteIp, remote.getType());
			} else if (role != null && role.endsWith("Server")) {
				addCallProject(remoteIp, remote.getType());
			} else if (role != null && role.endsWith("Caller")) {
				addCallerProject(remoteIp, remote.getType());
			}
		}
	}

}
