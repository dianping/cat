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
package com.dianping.cat.system.page.config;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	private SystemPage m_page;

	@ObjectMeta("project")
	private Project m_project = new Project();

	@ObjectMeta("domainConfig")
	private DomainConfig m_domainConfig = new DomainConfig();

	@ObjectMeta("edgeConfig")
	private EdgeConfig m_edgeConfig = new EdgeConfig();

	@ObjectMeta("exceptionLimit")
	private ExceptionLimit m_exceptionLimit = new ExceptionLimit();

	@ObjectMeta("exceptionExclude")
	private ExceptionExclude m_exceptionExclude = new ExceptionExclude();

	@FieldMeta("pars")
	private String m_pars;

	@FieldMeta("projectId")
	private int m_projectId;

	@FieldMeta("productLineName")
	private String m_productLineName;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("domains")
	private String[] m_domains = new String[100];

	@FieldMeta("from")
	private String m_from;

	@FieldMeta("id")
	private int m_id;

	@FieldMeta("metricKey")
	private String m_metricKey;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("to")
	private String m_to;

	@FieldMeta("pattern")
	private String m_pattern;

	@FieldMeta("exception")
	private String m_exception;

	@FieldMeta(Constants.REPORT_BUG)
	private String m_bug;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("allOnOrOff")
	private String m_allOnOrOff;

	@FieldMeta("ruleId")
	private String m_ruleId;

	@FieldMeta("available")
	private Boolean m_available;

	@FieldMeta("metrics")
	private String m_metrics;

	@FieldMeta("configs")
	private String m_configs;

	@FieldMeta("countTags")
	private String m_countTags;

	@FieldMeta("avgTags")
	private String m_avgTags;

	@FieldMeta("sumTags")
	private String m_sumTags;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("title")
	private String m_title;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.PROJECT_ALL;
		}
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.PROJECT_ALL);
	}

	public String getAllOnOrOff() {
		return m_allOnOrOff;
	}

	public void setAllOnOrOff(String allOnOrOff) {
		m_allOnOrOff = allOnOrOff;
	}

	public String getAvgTags() {
		return m_avgTags;
	}

	public void setAvgTags(String avgTags) {
		m_avgTags = avgTags;
	}

	public String getBug() {
		return m_bug;
	}

	public void setBug(String bug) {
		m_bug = bug;
	}

	public String getConfigs() {
		return m_configs;
	}

	public void setConfigs(String configs) {
		m_configs = configs;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public String getCoungTags() {
		return m_countTags;
	}

	public void setCoungTags(String coungTags) {
		m_countTags = coungTags;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public DomainConfig getDomainConfig() {
		return m_domainConfig;
	}

	public void setDomainConfig(DomainConfig domainConfig) {
		m_domainConfig = domainConfig;
	}

	public String[] getDomains() {
		return m_domains;
	}

	public void setDomains(String[] domains) {
		m_domains = domains;
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
	}

	public String getException() {
		return m_exception;
	}

	public void setException(String exception) {
		m_exception = exception;
	}

	public ExceptionExclude getExceptionExclude() {
		return m_exceptionExclude;
	}

	public ExceptionLimit getExceptionLimit() {
		return m_exceptionLimit;
	}

	public void setExceptionLimit(ExceptionLimit exceptionLimit) {
		m_exceptionLimit = exceptionLimit;
	}

	public String getFrom() {
		return m_from;
	}

	public void setFrom(String from) {
		m_from = from;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	public String getMetricKey() {
		if (m_metricKey != null) {
			m_metricKey = m_metricKey.trim();
		}
		return m_metricKey;
	}

	public void setMetricKey(String metricKey) {
		m_metricKey = metricKey;
	}

	public String getMetrics() {
		return m_metrics;
	}

	public void setMetrics(String metrics) {
		m_metrics = metrics;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.CONFIG);
	}

	public String getPars() {
		return m_pars;
	}

	public void setPars(String pars) {
		m_pars = pars;
	}

	public String getPattern() {
		return m_pattern;
	}

	public void setPattern(String pattern) {
		m_pattern = pattern;
	}

	public String getProductLineName() {
		return m_productLineName;
	}

	public void setProductLineName(String productLineName) {
		m_productLineName = productLineName;
	}

	public Project getProject() {
		return m_project;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public int getProjectId() {
		return m_projectId;
	}

	public void setProjectId(int id) {
		m_projectId = id;
	}

	public String getReportType() {
		return "";
	}

	public String getRuleId() {
		return m_ruleId;
	}

	public void setRuleId(String ruleId) {
		m_ruleId = ruleId;
	}

	public Boolean getAvailable() {
		return m_available;
	}

	public void setAvailable(Boolean available) {
		m_available = available;
	}

	public String getSumTags() {
		return m_sumTags;
	}

	public void setSumTags(String sumTags) {
		m_sumTags = sumTags;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getTo() {
		return m_to;
	}

	public void setTo(String to) {
		m_to = to;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		if (type.startsWith("Cache.")) {
			type = "Cache";
		}
		if (type.equals("Call")) {
			type = "PigeonCall";
		}
		if (type.equals("Service")) {
			type = "PigeonService";
		}
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}

}
