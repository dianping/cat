package com.dianping.cat.system.page.project;

import com.dianping.cat.core.dal.Project;
import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@ObjectMeta("project")
	private Project m_project = new Project();

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public Project getProject() {
		return m_project;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.DOMAINS);
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.PROJECT);
	}

	public void setProject(Project project) {
		m_project = project;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.DOMAINS;
		}
	}
}
