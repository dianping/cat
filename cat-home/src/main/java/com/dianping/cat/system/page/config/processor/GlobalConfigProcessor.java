package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Par;
import com.dianping.cat.home.alert.thirdparty.entity.Socket;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.SenderConfigManager;
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

	@Inject
	private SenderConfigManager m_senderConfigManager;

	private boolean deleteProject(Payload payload) {
		Project proto = new Project();
		int id = payload.getProjectId();

		proto.setId(id);
		proto.setKeyId(id);
		return m_projectService.deleteProject(proto);
	}

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case PROJECT_ALL:
			String domain = payload.getDomain();

			if (StringUtils.isEmpty(domain)) {
				domain = Constants.CAT;
			}
			model.setProjects(queryAllProjects());
			model.setProject(m_projectService.findByDomain(domain));
			break;
		case PROJECT_UPDATE_SUBMIT:
			model.setOpState(updateProject(payload));
			domain = payload.getDomain();

			if (StringUtils.isEmpty(domain)) {
				domain = payload.getProject().getDomain();

				if (StringUtils.isEmpty(domain)) {
					domain = Constants.CAT;
				}
			}
			model.setProjects(queryAllProjects());
			model.setProject(m_projectService.findByDomain(domain));
			break;
		case PROJECT_DELETE:
			model.setOpState(deleteProject(payload));
			domain = payload.getDomain();

			if (StringUtils.isEmpty(domain)) {
				domain = Constants.CAT;
			}
			model.setProjects(queryAllProjects());
			model.setProject(m_projectService.findByDomain(domain));
			break;
		case DOMAIN_GROUP_CONFIGS:
			model.setDomainGroup(m_domainGroupConfigManger.getDomainGroup());
			break;
		case DOMAIN_GROUP_CONFIG_UPDATE:
			domain = payload.getDomain();
			Domain groupDomain = m_domainGroupConfigManger.queryGroupDomain(domain);

			model.setGroupDomain(groupDomain);
			break;
		case DOMAIN_GROUP_CONFIG_DELETE:
			m_domainGroupConfigManger.deleteGroup(payload.getDomain());
			model.setDomainGroup(m_domainGroupConfigManger.getDomainGroup());
			break;
		case DOMAIN_GROUP_CONFIG_SUBMIT:
			m_domainGroupConfigManger.insertFromJson(payload.getContent());
			model.setDomainGroup(m_domainGroupConfigManger.getDomainGroup());
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
		case THIRD_PARTY_RULE_CONFIGS:
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		case THIRD_PARTY_RULE_UPDATE:
			Pair<Http, Socket> pair = queryThirdPartyConfigInfo(payload);

			if (pair != null) {
				model.setHttp(pair.getKey());
				model.setSocket(pair.getValue());
			}
			break;
		case THIRD_PARTY_RULE_SUBMIT:
			String type = payload.getType();

			if ("http".equals(type)) {
				m_thirdPartyConfigManager.insert(buildHttp(payload));
			}
			if ("socket".equals(type)) {
				m_thirdPartyConfigManager.insert(payload.getSocket());
			}
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		case THIRD_PARTY_RULE_DELETE:
			type = payload.getType();
			String ruleId = payload.getRuleId();

			m_thirdPartyConfigManager.remove(ruleId, type);
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		case ROUTER_CONFIG_UPDATE:
			String routerConfig = payload.getContent();
			if (!StringUtils.isEmpty(routerConfig)) {
				model.setOpState(m_routerConfigManager.insert(routerConfig));
			}
			model.setContent(m_routerConfigManager.getRouterConfig().toString());
			break;
		case ALERT_SENDER_CONFIG_UPDATE:
			String senderConfig = payload.getContent();
			if (!StringUtils.isEmpty(senderConfig)) {
				model.setOpState(m_senderConfigManager.insert(senderConfig));
			}
			model.setContent(m_senderConfigManager.getConfig().toString());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private Http buildHttp(Payload payload) {
		Http http = payload.getHttp();
		String[] pars = payload.getPars().split(",");
		List<Par> lst = new ArrayList<Par>();

		for (int i = 0; i < pars.length; i++) {
			if (StringUtils.isNotEmpty(pars[i])) {
				Par par = new Par();
				String id = pars[i].trim();

				if (!id.contains("=")) {
					Par p = lst.get(lst.size() - 1);

					p.setId(p.getId() + "," + id);
				} else {
					par.setId(id);
					lst.add(par);
				}
			}
		}
		for (Par p : lst) {
			http.addPar(p);
		}
		return http;
	}

	private Pair<Http, Socket> queryThirdPartyConfigInfo(Payload payload) {
		String ruleId = payload.getRuleId();
		String type = payload.getType();
		Http http = null;
		Socket socket = null;

		if (StringUtils.isNotEmpty(ruleId)) {
			if ("http".equals(type)) {
				http = m_thirdPartyConfigManager.queryHttp(ruleId);
			} else if ("socket".equals(type)) {
				socket = m_thirdPartyConfigManager.querySocket(ruleId);
			}
			return new Pair<Http, Socket>(http, socket);
		}
		return null;
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

	private boolean updateProject(Payload payload) {
		Project project = payload.getProject();
		project.setKeyId(project.getId());

		boolean success = m_projectService.updateProject(project);
		if (success) {
			m_manager.getProjects().put(project.getDomain(), project);
		}
		return success;
	}

	public static class ProjectCompartor implements Comparator<Project> {

		@Override
		public int compare(Project o1, Project o2) {
			String department1 = o1.getBu();
			String department2 = o2.getBu();
			String productLine1 = o1.getCmdbProductline();
			String productLine2 = o2.getCmdbProductline();

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
