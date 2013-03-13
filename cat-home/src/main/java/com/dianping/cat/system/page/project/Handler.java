package com.dianping.cat.system.page.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dainping.cat.consumer.dal.report.Project;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ProjectEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.SystemPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private DomainNavManager m_domainNavManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "project")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "project")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.PROJECT);
		Action action = payload.getAction();

		model.setAction(action);
		switch (action) {
		case ALL:
			model.setProjects(queryAllProjects());
			break;
		case PROJECT_UPDATE:
			model.setProject(queryProjectById(payload.getProjectId()));
			break;
		case PROJECT_UPDATE_SUBMIT:
			updateProject(payload);
			model.setProjects(queryAllProjects());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	@SuppressWarnings("static-access")
   private void updateProject(Payload payload) {
		int projectId = payload.getProjectId();
		String department = payload.getDepartment();
		String email = payload.getEmail();
		String owner = payload.getOwner();
		String projectLine = payload.getProjectLine();
		String domain = payload.getDomain();

		Project project = m_projectDao.createLocal();
		project.setId(projectId);
		project.setKeyId(projectId);
		project.setDepartment(department);
		project.setEmail(email);
		project.setDomain(domain);
		project.setOwner(owner);
		project.setProjectLine(projectLine);

		try {
			m_projectDao.updateByPK(project, ProjectEntity.UPDATESET_FULL);
			m_domainNavManager.getProjects().put(project.getDomain(), project);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private List<Project> queryAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		try {
			projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		Collections.sort(projects, new ProjectCompartor());
		return projects;
	}

	private Project queryProjectById(int projectId) {
		Project project = null;
		try {
			project = m_projectDao.findByPK(projectId, ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return project;
	}

	class ProjectCompartor implements Comparator<Project> {

		@Override
		public int compare(Project o1, Project o2) {
			String department1 = o1.getDepartment();
			String department2 = o2.getDepartment();
			String productLine1 = o1.getProjectLine();
			String productLine2 = o2.getProjectLine();

			if (department1.equalsIgnoreCase(department2)) {
				if (productLine1.equalsIgnoreCase(productLine2)) {
					return o1.getDomain().compareTo(o2.getDomain());
				} else {
					return productLine1.compareTo(productLine2);
				}
			} else {
				return department1.compareTo(department2);
			}
		}

	}
}
