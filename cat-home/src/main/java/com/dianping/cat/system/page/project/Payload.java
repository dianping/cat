package com.dianping.cat.system.page.project;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("department")
	private String m_department;

	@FieldMeta("email")
	private String m_email;

	@FieldMeta("owner")
	private String m_owner;

	private SystemPage m_page;

	@FieldMeta("projectId")
	private int m_projectId;

	@FieldMeta("projectLine")
	private String m_projectLine;

	@FieldMeta("domain")
	private String m_domain;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.ALL;
		}
		return m_action;
	}

	public String getDepartment() {
		return m_department;
	}

	public String getEmail() {
		return m_email;
	}

	public String getOwner() {
		return m_owner;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public int getProjectId() {
		return m_projectId;
	}

	public String getProjectLine() {
		return m_projectLine;
	}

	public String getReportType() {
		return "";
	}

	public void setAction(String action) {
		m_action =Action.getByName(action, Action.ALL);
	}

	public void setDepartment(String department) {
		m_department = department;
	}

	public void setEmail(String email) {
		m_email = email;
	}

	public void setOwner(String owner) {
		m_owner = owner;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.PROJECT);
	}

	public void setProjectId(int projectId) {
		m_projectId = projectId;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setProjectLine(String projectLine) {
		m_projectLine = projectLine;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
