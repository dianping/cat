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
package com.dianping.cat.system;

import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.annotation.ModuleMeta;

public enum SystemPage implements Page {

	LOGIN("login", "login", "Login", "Login", false),

	CONFIG("config", "config", "Config", "Config", false),

	PLUGIN("plugin", "plugin", "Plugin", "Plugin", true),

	ROUTER("router", "router", "Router", "Router", true),

	WEB("web", "web", "Web", "Web", true),

	PROJECT("project", "project", "Project", "Project", true),

	APP("app", "app", "App", "App", true),

	BUSINESS("business", "business", "Business", "Business", true),

	PERMISSION("permission", "permission", "Permission", "Permission", true);

	private String m_name;

	private String m_path;

	private String m_title;

	private String m_description;

	private boolean m_standalone;

	private SystemPage(String name, String path, String title, String description, boolean standalone) {
		m_name = name;
		m_path = path;
		m_title = title;
		m_description = description;
		m_standalone = standalone;
	}

	public static SystemPage getByName(String name, SystemPage defaultPage) {
		for (SystemPage action : SystemPage.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultPage;
	}

	public String getDescription() {
		return m_description;
	}

	public String getModuleName() {
		ModuleMeta meta = SystemModule.class.getAnnotation(ModuleMeta.class);

		if (meta != null) {
			return meta.name();
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getPath() {
		return m_path;
	}

	public String getTitle() {
		return m_title;
	}

	public boolean isStandalone() {
		return m_standalone;
	}

	public SystemPage[] getValues() {
		return SystemPage.values();
	}
}
