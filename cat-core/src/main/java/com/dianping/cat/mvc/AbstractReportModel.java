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
package com.dianping.cat.mvc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;

public abstract class AbstractReportModel<A extends Action, P extends Page, M extends ActionContext<?>>
						extends	ViewModel<P, A, M> {

	private transient Date m_creatTime;

	private transient String m_customDate;

	private transient long m_date;

	private transient SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private transient SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private transient String m_displayDomain;

	private transient Throwable m_exception;

	private transient String m_ipAddress;

	private transient String m_reportType;

	private transient ProjectService m_projectService;

	private transient HostinfoService m_hostInfoService;

	private transient SampleConfigManager m_sampleConfigManager;

	public AbstractReportModel(M ctx) {
		super(ctx);
		try {
			m_projectService = ContainerLoader.getDefaultContainer().lookup(ProjectService.class);
			m_hostInfoService = ContainerLoader.getDefaultContainer().lookup(HostinfoService.class);
			m_sampleConfigManager = ContainerLoader.getDefaultContainer().lookup(SampleConfigManager.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public double getSample() {
		Domain domain = m_sampleConfigManager.getConfig().findDomain(getDomain());

		if (domain != null) {
			return domain.getSample();
		} else {
			return 1.0;
		}
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	public Date getCreatTime() {
		return m_creatTime;
	}

	public void setCreatTime(Date creatTime) {
		m_creatTime = creatTime;
	}

	// required by current tag()
	public HistoryNav getCurrentNav() {
		return HistoryNav.getByName(m_reportType);
	}

	// required by report tag
	public Date getCurrentTime() {
		return new Date();
	}

	public String getCustomDate() {
		return m_customDate;
	}

	// required by report tag
	public String getDate() {
		if (m_reportType != null && m_reportType.length() > 0) {
			return m_dayFormat.format(new Date(m_date));
		}
		return m_dateFormat.format(new Date(m_date));
	}

	public void setDate(long date) {
		m_date = date;
	}

	public String getDisplayDomain() {
		return m_displayDomain;
	}

	public void setDisplayDomain(String displayDomain) {
		m_displayDomain = displayDomain;
	}

	public String getDisplayHour() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			return "0" + Integer.toString(hour);
		} else {
			return Integer.toString(hour);
		}
	}

	public abstract String getDomain();

	public Map<String, Department> getDomainGroups() {
		return m_projectService.findDepartments(getDomains());
	}

	public Collection<String> getDomains() {
		return m_projectService.findAllDomains();
	}

	// required by report tag
	public Throwable getException() {
		return m_exception;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	;

	// required by report tag
	// required by report history tag
	public HistoryNav[] getHistoryNavs() {
		return HistoryNav.values();
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public List<String> getIps() {
		return new ArrayList<String>();
	}

	public Map<String, String> getIpToHostname() {
		List<String> ips = getIps();
		Map<String, String> ipToHostname = new HashMap<String, String>();

		for (String ip : ips) {
			String hostname = m_hostInfoService.queryHostnameByIp(ip);

			if (hostname != null && !hostname.equalsIgnoreCase("null")) {
				ipToHostname.put(ip, hostname);
			}
		}

		return ipToHostname;
	}

	public String getIpToHostnameStr() {
		return new JsonBuilder().toJson(getIpToHostname());
	}

	public long getLongDate() {
		return m_date;
	}

	// required by report tag
	public UrlNav[] getNavs() {
		return UrlNav.values();
	}

	public String getReportType() {
		return m_reportType;
	}

	public void setReportType(String reportType) {
		m_reportType = reportType;
	}

	public void setCustomDate(Date start, Date end) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		StringBuilder sb = new StringBuilder();

		sb.append("&startDate=").append(sdf.format(start)).append("&endDate=").append(sdf.format(end));
		m_customDate = sb.toString();
	}
}
