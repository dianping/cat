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
package com.dianping.cat.system.page.plugin;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@PathMeta("parts")
	private String[] m_parts;

	@FieldMeta("source")
	private boolean m_downloadSource;

	@FieldMeta("file")
	private String m_file;

	@FieldMeta("mapping")
	private boolean m_downloadMapping;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.PLUGIN);
	}

	public String getType() {
		if (m_parts != null && m_parts.length > 0) {
			return m_parts[0];
		} else {
			return null;
		}
	}

	public boolean isDownloadMapping() {
		return m_downloadMapping;
	}

	public boolean isDownloadSource() {
		return m_downloadSource;
	}

	public String getFile() {
		return m_file;
	}

	public void setFile(String file) {
		m_file = file;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
