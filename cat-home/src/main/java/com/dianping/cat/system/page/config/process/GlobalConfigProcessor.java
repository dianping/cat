package com.dianping.cat.system.page.config.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.ThirdPartyConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class GlobalConfigProcessor {

	@Inject
	public ProjectService m_projectService;

	@Inject
	public DomainNavManager m_manager;

	@Inject
	private BugConfigManager m_bugConfigManager;

	@Inject
	private ThirdPartyConfigManager m_thirdPartyConfigManager;

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Inject
	private DomainGroupConfigManager m_domainGroupConfigManger;

	private void deleteProject(Payload payload) {
		Project proto = new Project();
		int id = payload.getProjectId();

		proto.setId(id);
		proto.setKeyId(id);
		m_projectService.deleteProject(proto);
	}

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case PROJECT_ALL:
			model.setProjects(queryAllProjects());
			break;
		case PROJECT_UPDATE:
			model.setProject(queryProjectById(payload.getProjectId()));
			break;
		case PROJECT_UPDATE_SUBMIT:
			updateProject(payload);
			model.setProjects(queryAllProjects());
			break;
		case PROJECT_DELETE:
			deleteProject(payload);
			model.setProjects(queryAllProjects());
			break;
		case DOMAIN_GROUP_CONFIG_UPDATE:
			String domainGroupContent = payload.getContent();
			if (!StringUtils.isEmpty(domainGroupContent)) {
				model.setOpState(m_domainGroupConfigManger.insert(domainGroupContent));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_domainGroupConfigManger.getDomainGroup().toString());
			break;
		case BUG_CONFIG_UPDATE:
			String xml = payload.getBug();
			if (!StringUtils.isEmpty(xml)) {
				model.setOpState(m_bugConfigManager.insert(xml));
			} else {
				model.setOpState(true);
			}
			model.setBug(m_bugConfigManager.getBugConfig().toString());
			break;
		case THIRD_PARTY_CONFIG_UPDATE:
			String thirdPartyConfig = payload.getContent();
			if (!StringUtils.isEmpty(thirdPartyConfig)) {
				model.setOpState(m_thirdPartyConfigManager.insert(thirdPartyConfig));
			}
			model.setContent(m_thirdPartyConfigManager.getConfig().toString());
			break;
		case ROUTER_CONFIG_UPDATE:
			String routerConfig = payload.getContent();
			if (!StringUtils.isEmpty(routerConfig)) {
				model.setOpState(m_routerConfigManager.insert(routerConfig));
			}
			model.setContent(m_routerConfigManager.getRouterConfig().toString());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	public List<Project> queryAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		try {
			projects = m_projectService.findAll();
		} catch (Exception e) {
			Cat.logError(e);
		}
		Collections.sort(projects, new ProjectCompartor());
		return projects;
	}

	public List<String> queryDoaminList() {
		List<String> result = new ArrayList<String>();
		List<Project> projects = queryAllProjects();

		result.add("Default");
		for (Project p : projects) {
			result.add(p.getDomain());
		}
		return result;
	}

	private Project queryProjectById(int projectId) {
		Project project = null;
		try {
			project = m_projectService.findProject(projectId);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return project;
	}

	private void updateProject(Payload payload) {
		Project project = payload.getProject();
		project.setKeyId(project.getId());

		m_projectService.updateProject(project);
		m_manager.getProjects().put(project.getDomain(), project);
	}

	public static class ProjectCompartor implements Comparator<Project> {

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
