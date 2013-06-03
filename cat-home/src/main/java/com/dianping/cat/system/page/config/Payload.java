package com.dianping.cat.system.page.config;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.consumer.core.dal.Project;
import com.dianping.cat.home.dal.report.AggregationRule;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	private SystemPage m_page;

	@ObjectMeta("project")
	private Project m_project = new Project();

	@ObjectMeta("aggregation")
	private AggregationRule m_rule = new AggregationRule();

	@ObjectMeta("domainConfig")
	private DomainConfig m_domainConfig = new DomainConfig();

	@ObjectMeta("edgeConfig")
	private EdgeConfig m_edgeConfig = new EdgeConfig();

	@FieldMeta("projectId")
	private int m_projectId;

	@FieldMeta("id")
	private int m_id;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.PROJECT_ALL;
		}
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getReportType() {
		return "";
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.PROJECT_ALL);
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.CONFIG);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}

	public void setProjectId(int id) {
		m_projectId = id;
	}

	public AggregationRule getRule() {
		return m_rule;
	}

	public void setRule(AggregationRule rule) {
		m_rule = rule;
	}

	public DomainConfig getDomainConfig() {
		return m_domainConfig;
	}

	public void setDomainConfig(DomainConfig domainConfig) {
		m_domainConfig = domainConfig;
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
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

	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}

}
