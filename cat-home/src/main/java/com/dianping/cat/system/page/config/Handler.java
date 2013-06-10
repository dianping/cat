package com.dianping.cat.system.page.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.hsqldb.lib.StringUtil;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.dal.Project;
import com.dianping.cat.consumer.core.dal.ProjectDao;
import com.dianping.cat.consumer.core.dal.ProjectEntity;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.AggregationRule;
import com.dianping.cat.home.dal.report.AggregationRuleDao;
import com.dianping.cat.home.dal.report.AggregationRuleEntity;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.ProductLine;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.SystemPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private AggregationRuleDao m_aggregationRuleDao;

	@Inject
	private TopologyGraphConfigManager m_topologyConfigManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "config")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "config")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.CONFIG);
		Action action = payload.getAction();

		model.setAction(action);
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

		case AGGREGATION_ALL:
			model.setAggregationRules(queryAllAggregationRules());
			break;
		case AGGREGATION_UPDATE:
			model.setAggregationRule(queryAggregationRuleById(payload.getId()));
			break;
		case AGGREGATION_UPDATE_SUBMIT:
			updateAggregationRule(payload);
			model.setAggregationRules(queryAllAggregationRules());
			break;
		case AGGREGATION_DELETE:
			deleteAggregationRule(payload);
			model.setAggregationRules(queryAllAggregationRules());
			break;

		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
			graphNodeConfigAddOrUpdate(payload, model);
			model.setProjects(queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphNodeConfigAddOrUpdateSubmit(payload, model));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
			model.setOpState(graphNodeConfigDelete(payload));
			model.setConfig(m_topologyConfigManager.getConfig());
			break;

		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.buildEdgeInfo();
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
			graphEdgeConfigAdd(payload, model);
			model.setProjects(queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphEdgeConfigAddOrUpdateSubmit(payload, model));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.buildEdgeInfo();
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.setOpState(graphEdgeConfigDelete(payload));
			model.buildEdgeInfo();
			break;

		case TOPOLOGY_GRAPH_PRODUCT_LINE:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
			graphPruductLineAddOrUpdate(payload, model);
			model.setProjects(queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
			model.setOpState(m_topologyConfigManager.deleteProductLine(payload.getProductLineName()));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphProductLineConfigAddOrUpdateSubmit(payload, model));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private boolean graphProductLineConfigAddOrUpdateSubmit(Payload payload, Model model) {
		ProductLine line = payload.getProductLine();
	 String[] domains = payload.getDomains();

		return m_topologyConfigManager.insertProductLine(line, domains);
	}

	private void graphPruductLineAddOrUpdate(Payload payload, Model model) {
		String name = payload.getProductLineName();

		if (!StringUtil.isEmpty(name)) {
			model.setProductLine(m_topologyConfigManager.getConfig().findProductLine(name));
		}
	}

	private boolean graphEdgeConfigAddOrUpdateSubmit(Payload payload, Model model) {
		EdgeConfig config = payload.getEdgeConfig();

		if (!StringUtil.isEmpty(config.getType())) {
			model.setEdgeConfig(config);
			payload.setType(config.getType());
			return m_topologyConfigManager.insertEdgeConfig(config);
		} else {
			return false;
		}
	}

	private void graphNodeConfigAddOrUpdate(Payload payload, Model model) {
		String domain = payload.getDomain();
		String type = payload.getType();

		if (!StringUtils.isEmpty(domain)) {
			model.setDomainConfig(m_topologyConfigManager.queryNodeConfig(type, domain));
		}
	}

	private boolean graphNodeConfigAddOrUpdateSubmit(Payload payload, Model model) {
		String type = payload.getType();
		DomainConfig config = payload.getDomainConfig();
		String domain = config.getId();
		model.setDomainConfig(config);

		if (domain.equalsIgnoreCase(CatString.ALL)) {
			return m_topologyConfigManager.insertDomainDefaultConfig(type, config);
		} else {
			return m_topologyConfigManager.insertDomainConfig(type, config);
		}
	}

	private boolean graphNodeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteDomainConfig(payload.getType(), payload.getDomain());
	}

	private void graphEdgeConfigAdd(Payload payload, Model model) {
		String type = payload.getType();
		String from = payload.getFrom();
		String to = payload.getTo();
		EdgeConfig config = m_topologyConfigManager.queryEdgeConfig(type, from, to);

		model.setEdgeConfig(config);
	}

	private boolean graphEdgeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteEdgeConfig(payload.getType(), payload.getFrom(), payload.getTo());
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

	private void updateProject(Payload payload) {
		Project project = payload.getProject();
		project.setKeyId(project.getId());

		try {
			m_projectDao.updateByPK(project, ProjectEntity.UPDATESET_FULL);
			DomainNavManager.getProjects().put(project.getDomain(), project);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private void updateAggregationRule(Payload payload) {
		AggregationRule proto = payload.getRule();
		proto.setKeyId(payload.getId());
		try {
			if (proto.getKeyId() == 0) {
				m_aggregationRuleDao.insert(proto);
			} else {
				m_aggregationRuleDao.updateByPK(proto, AggregationRuleEntity.UPDATESET_FULL);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private List<AggregationRule> queryAllAggregationRules() {
		List<AggregationRule> aggregationRules = new ArrayList<AggregationRule>();
		try {
			aggregationRules = m_aggregationRuleDao.findAll(AggregationRuleEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return aggregationRules;
	}

	private AggregationRule queryAggregationRuleById(int id) {
		try {
			return m_aggregationRuleDao.findByPK(id, AggregationRuleEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
			return null;
		}
	}

	private void deleteAggregationRule(Payload payload) {
		AggregationRule proto = new AggregationRule();
		proto.setKeyId(payload.getId());
		try {
			m_aggregationRuleDao.deleteByPK(proto);
		} catch (DalException e) {
			Cat.logError(e);
		}
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
