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
package com.dianping.cat.system.page.business;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.system.SystemPage;

@ModelMeta("model")
public class Model extends ViewModel<SystemPage, Action, Context> {

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private String m_domain;

	private Set<String> m_domains;

	private Map<String, Set<String>> m_tags;

	private List<BusinessItemConfig> m_configs;

	private List<CustomConfig> m_customConfigs;

	private BusinessItemConfig m_businessItemConfig;

	private CustomConfig m_customConfig;

	private String m_content;

	private String m_attributes;

	private String m_id;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.LIST;
	}

	public String getOpState() {
		return m_opState;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public String getAttributes() {
		return m_attributes;
	}

	public void setAttributes(String attributes) {
		m_attributes = attributes;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public Set<String> getDomains() {
		return m_domains;
	}

	public void setDomains(Set<String> domains) {
		m_domains = domains;
	}

	public List<BusinessItemConfig> getConfigs() {
		return m_configs;
	}

	public void setConfigs(List<BusinessItemConfig> configs) {
		m_configs = configs;
	}

	public List<CustomConfig> getCustomConfigs() {
		return m_customConfigs;
	}

	public void setCustomConfigs(List<CustomConfig> customConfigs) {
		m_customConfigs = customConfigs;
	}

	public Map<String, Set<String>> getTags() {
		return m_tags;
	}

	public void setTags(Map<String, Set<String>> tags) {
		m_tags = tags;
	}

	public BusinessItemConfig getBusinessItemConfig() {
		return m_businessItemConfig;
	}

	public void setBusinessItemConfig(BusinessItemConfig businessItemConfig) {
		m_businessItemConfig = businessItemConfig;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public CustomConfig getCustomConfig() {
		return m_customConfig;
	}

	public void setCustomConfig(CustomConfig customConfig) {
		m_customConfig = customConfig;
	}

	public String getIpAddress() {
		return "";
	}

	public String getDate() {
		return "";
	}
}
