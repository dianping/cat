package com.dianping.cat.system.page.project;

import java.util.Collections;
import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dainping.cat.consumer.core.dal.Project;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Project m_project;

	private List<Project> m_projects;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDate() {
		return "";
	}

	public String getIpAddress() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.ALL;
	}

	public String getDomain() {
		return "";
	}

	public List<String> getDomains() {
		return Collections.emptyList();
	}

	public Project getProject() {
		return m_project;
	}

	public List<Project> getProjects() {
		return m_projects;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public void setProjects(List<Project> projects) {
		m_projects = projects;
	}
}
