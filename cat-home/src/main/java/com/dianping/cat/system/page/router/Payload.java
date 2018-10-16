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
package com.dianping.cat.system.page.router;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("date")
	private String m_date;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.API);
	}

	public Date getDate() {
		try {
			return m_sdf.parse(m_date);
		} catch (Exception e) {
			return TimeHelper.getCurrentDay(-1);
		}
	}

	public void setDate(String date) {
		m_date = date;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.ROUTER);
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.API;
		}
	}

}
