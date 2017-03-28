package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.BlackListManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.report.alert.sender.config.SenderConfigManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.statistics.config.BugConfigManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

public class GlobalConfigProcessor {

	@Inject
	public ProjectService m_projectService;

	@Inject
	private BugConfigManager m_bugConfigManager;

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Inject
	private DomainGroupConfigManager m_domainGroupConfigManger;

	@Inject
	private SenderConfigManager m_senderConfigManager;

	@Inject
	private BlackListManager m_blackListManager;

	@Inject
	private StorageGroupConfigManager m_groupConfigManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private AllReportConfigManager m_transactionConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	private RouterConfigHandler m_routerConfigHandler;

	private boolean deleteProject(Payload payload) {
		Project proto = new Project();
		int id = payload.getProjectId();

		proto.setId(id);
		proto.setKeyId(id);
		return m_projectService.delete(proto);
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
			Project pro = payload.getProject();

			if (pro.getId() > 0) {
				model.setOpState(updateProject(payload));
			} else {
				try {
					m_projectService.insert(pro);
					model.setOpState(true);
				} catch (DalException e) {
					model.setOpState(false);
				}
			}
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
			model.setBug(m_configHtmlParser.parse(m_bugConfigManager.getBugConfig().toString()));
			break;
		case ROUTER_CONFIG_UPDATE:
			String routerConfig = payload.getContent();
			if (!StringUtils.isEmpty(routerConfig)) {
				model.setOpState(m_routerConfigManager.insert(routerConfig));
				m_routerConfigHandler.updateRouterConfig(TimeHelper.getCurrentDay(-1));
			}
			model.setContent(m_configHtmlParser.parse(m_routerConfigManager.getRouterConfig().toString()));
			break;
		case ALERT_SENDER_CONFIG_UPDATE:
			String senderConfig = payload.getContent();
			if (!StringUtils.isEmpty(senderConfig)) {
				model.setOpState(m_senderConfigManager.insert(senderConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_senderConfigManager.getConfig().toString()));
			break;
		case BLACK_CONFIG_UPDATE:
			String blackConfig = payload.getContent();

			if (!StringUtils.isEmpty(blackConfig)) {
				model.setOpState(m_blackListManager.insert(blackConfig));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_blackListManager.getBlackList().toString()));
			break;
		case STORAGE_GROUP_CONFIG_UPDATE:
			String storageGroup = payload.getContent();
			if (!StringUtils.isEmpty(storageGroup)) {
				model.setOpState(m_groupConfigManager.insert(storageGroup));
			}
			model.setContent(m_configHtmlParser.parse(m_groupConfigManager.getConfig().toString()));
			break;
		case SERVER_FILTER_CONFIG_UPDATE:
			String serverConfig = payload.getContent();
			if (!StringUtils.isEmpty(serverConfig)) {
				model.setOpState(m_serverFilterConfigManager.insert(serverConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_serverFilterConfigManager.getConfig().toString()));
			break;
		case ALL_REPORT_CONFIG:
			String transactionConfig = payload.getContent();
			if (!StringUtils.isEmpty(transactionConfig)) {
				model.setOpState(m_transactionConfigManager.insert(transactionConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_transactionConfigManager.getConfig().toString()));
			break;
		default:
			break;
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

	private boolean updateProject(Payload payload) {
		Project project = payload.getProject();
		project.setKeyId(project.getId());

		return m_projectService.update(project);
	}

	public static class ProjectCompartor implements Comparator<Project> {

		@Override
		public int compare(Project o1, Project o2) {
			String department1 = String.valueOf(o1.getBu());
			String department2 = String.valueOf(o2.getBu());
			String productLine1 = String.valueOf(o1.getCmdbProductline());
			String productLine2 = String.valueOf(o2.getCmdbProductline());

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
