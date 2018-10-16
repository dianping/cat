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
package com.dianping.cat.system.page.login;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("rtnUrl")
	private String m_rtnUrl;

	@FieldMeta("account")
	private String m_account;

	@FieldMeta("password")
	private String m_password;

	@FieldMeta("login")
	private boolean m_submit;

	public String getAccount() {
		return m_account;
	}

	public void setAccount(String account) {
		m_account = account;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LOGIN);
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.LOGIN);
	}

	public String getPassword() {
		return m_password;
	}

	public void setPassword(String password) {
		m_password = password;
	}

	public String getRtnUrl() {
		return m_rtnUrl;
	}

	public void setRtnUrl(String rtnUrl) {
		m_rtnUrl = rtnUrl;
	}

	public boolean isSubmit() {
		return m_submit;
	}

	public void setSubmit(boolean submit) {
		m_submit = submit;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LOGIN;
		}
	}
}
