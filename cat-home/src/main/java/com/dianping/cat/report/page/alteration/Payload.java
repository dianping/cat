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
package com.dianping.cat.report.page.alteration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	private ReportPage m_page;

	@FieldMeta("altType")
	private String m_altType;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("alterationDate")
	private String m_alterationDate;

	@FieldMeta("user")
	private String m_user;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("startTime")
	private String m_startTime;

	@FieldMeta("endTime")
	private String m_endTime;

	@FieldMeta("hostname")
	private String m_hostname;

	@FieldMeta("count")
	private int m_count;

	@FieldMeta("status")
	private int m_status;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private SimpleDateFormat m_secondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Payload() {
		super(ReportPage.ALTERATION);
	}

	@Override
	public Action getAction() {
		if (m_action == null) {
			return Action.VIEW;
		} else {
			return m_action;
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public Date getAlterationDate() {
		try {
			if (m_alterationDate.length() == 16) {
				return m_sdf.parse(m_alterationDate);
			} else {
				return m_secondFormat.parse(m_alterationDate);
			}

		} catch (ParseException e) {
			return new Date();
		}
	}

	public void setAlterationDate(String alterationDate) {
		m_alterationDate = alterationDate;
	}

	public String getAltType() {
		return m_altType;
	}

	public void setAltType(String altType) {
		m_altType = altType;
	}

	public String[] getAltTypeArray() {
		if (StringUtils.isEmpty(m_altType)) {
			return null;
		} else {
			return m_altType.split(",");
		}
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public int getCount() {
		if (m_count == 0) {
			return 10;
		} else {
			return m_count;
		}
	}

	public void setCount(int count) {
		m_count = count;
	}

	public String getDomain() {
		if (m_domain == null || "".equals(m_domain)) {
			return null;
		} else {
			return m_domain;
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public Date getEndTime() {
		if (m_endTime == null || m_endTime.length() == 0) {
			return new Date();
		} else {
			try {
				return m_sdf.parse(m_endTime);
			} catch (ParseException e) {
				return new Date();
			}
		}
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public String getGroup() {
		return m_group;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public String getHostname() {
		if (m_hostname == null || "".equals(m_hostname)) {
			return null;
		} else {
			return m_hostname;
		}
	}

	public void setHostname(String hostname) {
		m_hostname = hostname;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ALTERATION);
	}

	public Date getStartTime() {
		if (m_startTime == null || m_startTime.length() == 0) {
			return new Date(System.currentTimeMillis() - TimeHelper.ONE_HOUR / 4);
		} else {
			try {
				return m_sdf.parse(m_startTime);
			} catch (ParseException e) {
				return new Date();
			}
		}
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public int getStatus() {
		return m_status;
	}

	public void setStatus(int status) {
		m_status = status;
	}

	public String getTitle() {
		if (m_title != null && m_title.length() > 128) {
			m_title = m_title.substring(0, 128);
		}

		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	public String getUser() {
		return m_user;
	}

	public void setUser(String user) {
		m_user = user;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
