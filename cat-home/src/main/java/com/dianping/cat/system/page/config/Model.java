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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.processor.BaseProcesser.RuleItem;

@ModelMeta("model")
public class Model extends ViewModel<SystemPage, Action, Context> {

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private Project m_project;

	private List<Project> m_projects;

	private ExceptionLimit m_exceptionLimit;

	private List<ExceptionLimit> m_exceptionLimits;

	private ExceptionExclude m_exceptionExclude;

	private List<ExceptionExclude> m_exceptionExcludes;

	private String m_opState = SUCCESS;

	private TopologyGraphConfig m_config;

	private Map<String, Edge> m_edgeConfigs = new HashMap<String, Edge>();

	private DomainConfig m_domainConfig;

	private EdgeConfig m_edgeConfig;

	private String m_bug;

	private String m_content;

	private String m_metricItemConfigRule;

	private List<String> m_domainList;

	private List<String> m_exceptionList;

	private List<RuleItem> m_ruleItems;

	private Collection<Rule> m_rules;

	private String m_id;

	private Boolean m_available = true;

	private String m_duplicateDomains;

	private List<String> m_tags;

	private String m_configHeader;

	private String m_domain;

	private List<String> m_heartbeatExtensionMetrics;

	private DomainGroup m_domainGroup;

	private com.dianping.cat.home.group.entity.Domain m_groupDomain;

	public Model(Context ctx) {
		super(ctx);
	}

	public void buildEdgeInfo() {
		Map<String, EdgeConfig> edges = m_config.getEdgeConfigs();

		for (EdgeConfig edge : edges.values()) {
			String type = edge.getType();
			Edge temp = m_edgeConfigs.get(type);

			if (temp == null) {
				List<EdgeConfig> edgeConfigs = new ArrayList<EdgeConfig>();
				temp = new Edge(edgeConfigs, m_config.findNodeConfig(edge.getType()));
				m_edgeConfigs.put(type, temp);
			}
			temp.getEdgeConfigs().add(edge);
		}
	}

	public String getBug() {
		return m_bug;
	}

	public void setBug(String bug) {
		m_bug = bug;
	}

	public TopologyGraphConfig getConfig() {
		return m_config;
	}

	public void setConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public String getConfigHeader() {
		return m_configHeader;
	}

	public void setConfigHeader(String configHeader) {
		m_configHeader = configHeader;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.PROJECT_ALL;
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

	public DomainGroup getDomainGroup() {
		return m_domainGroup;
	}

	public void setDomainGroup(DomainGroup domainGroup) {
		m_domainGroup = domainGroup;
	}

	public List<String> getDomainList() {
		return m_domainList;
	}

	public void setDomainList(List<String> domainList) {
		m_domainList = domainList;
	}

	public List<String> getDomains() {
		return Collections.emptyList();
	}

	public String getDuplicateDomains() {
		return m_duplicateDomains;
	}

	public void setDuplicateDomains(String duplicateDomains) {
		m_duplicateDomains = duplicateDomains;
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
	}

	public Map<String, Edge> getEdgeConfigs() {
		return m_edgeConfigs;
	}

	public ExceptionExclude getExceptionExclude() {
		return m_exceptionExclude;
	}

	public void setExceptionExclude(ExceptionExclude exceptionExclude) {
		m_exceptionExclude = exceptionExclude;
	}

	public List<ExceptionExclude> getExceptionExcludes() {
		return m_exceptionExcludes;
	}

	public void setExceptionExcludes(List<ExceptionExclude> exceptionExcludes) {
		m_exceptionExcludes = exceptionExcludes;
	}

	public ExceptionLimit getExceptionLimit() {
		return m_exceptionLimit;
	}

	public void setExceptionLimit(ExceptionLimit exceptionLimit) {
		m_exceptionLimit = exceptionLimit;
	}

	public List<ExceptionLimit> getExceptionLimits() {
		return m_exceptionLimits;
	}

	public void setExceptionLimits(List<ExceptionLimit> exceptionLimits) {
		m_exceptionLimits = exceptionLimits;
	}

	public List<String> getExceptionList() {
		return m_exceptionList;
	}

	public void setExceptionList(List<String> exceptionList) {
		m_exceptionList = exceptionList;
	}

	public com.dianping.cat.home.group.entity.Domain getGroupDomain() {
		return m_groupDomain;
	}

	public void setGroupDomain(com.dianping.cat.home.group.entity.Domain groupDomain) {
		m_groupDomain = groupDomain;
	}

	public List<String> getHeartbeatExtensionMetrics() {
		return m_heartbeatExtensionMetrics;
	}

	public void setHeartbeatExtensionMetrics(List<String> heartbeatExtensionMetrics) {
		m_heartbeatExtensionMetrics = heartbeatExtensionMetrics;
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public Boolean getAvailable() {
		return m_available;
	}

	public void setAvailable(Boolean available) {
		this.m_available = available;
	}

	public String getIpAddress() {
		return "";
	}

	public String getMetricItemConfigRule() {
		return m_metricItemConfigRule;
	}

	public void setMetricItemConfigRule(String metricItemConfigRule) {
		m_metricItemConfigRule = metricItemConfigRule;
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

	public Project getProject() {
		return m_project;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public List<Project> getProjects() {
		return m_projects;
	}

	public void setProjects(List<Project> projects) {
		m_projects = projects;
	}

	public String getReportType() {
		return "";
	}

	public List<RuleItem> getRuleItems() {
		return m_ruleItems;
	}

	public void setRuleItems(List<RuleItem> ruleItems) {
		m_ruleItems = ruleItems;
	}

	public Collection<Rule> getRules() {
		return m_rules;
	}

	public void setRules(Collection<Rule> rules) {
		m_rules = rules;
	}

	public List<String> getTags() {
		return m_tags;
	}

	public void setTags(List<String> tags) {
		m_tags = tags;
	}

	public void setGraphConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public static class Edge {
		private List<EdgeConfig> m_edgeConfigs;

		private NodeConfig m_nodeConfig;

		public Edge(List<EdgeConfig> edgeConfigs, NodeConfig nodeConfig) {
			m_edgeConfigs = edgeConfigs;
			m_nodeConfig = nodeConfig;
		}

		public List<EdgeConfig> getEdgeConfigs() {
			return m_edgeConfigs;
		}

		public NodeConfig getNodeConfig() {
			return m_nodeConfig;
		}
	}
}
