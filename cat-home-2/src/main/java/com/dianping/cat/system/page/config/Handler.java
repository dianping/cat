package com.dianping.cat.system.page.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.dianping.cat.Constants;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.aggreation.model.entity.AggregationRule;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.problem.aggregation.AggregationConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;
import com.dianping.cat.system.config.MetricGroupConfigManager;
import com.dianping.cat.system.config.UtilizationConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private TopologyGraphConfigManager m_topologyConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManger;

	@Inject
	private AggregationConfigManager m_aggreationConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private ExceptionThresholdConfigManager m_exceptionConfigManager;

	@Inject
	private UtilizationConfigManager m_utilizationConfigManager;

	@Inject
	private DomainGroupConfigManager m_domainGroupConfigManger;

	@Inject
	private BugConfigManager m_bugConfigManager;

	@Inject
	private MetricGroupConfigManager m_metricGroupConfigManager;
	
	@Inject
	private DomainNavManager m_manager;

	private void deleteAggregationRule(Payload payload) {
		m_aggreationConfigManager.deleteAggregationRule(payload.getPattern());
	}

	private void deleteExceptionLimit(Payload payload) {
		m_exceptionConfigManager.deleteExceptionLimit(payload.getDomain(), payload.getException());
	}

	private void graphEdgeConfigAdd(Payload payload, Model model) {
		String type = payload.getType();
		String from = payload.getFrom();
		String to = payload.getTo();
		EdgeConfig config = m_topologyConfigManager.queryEdgeConfig(type, from, to);

		model.setEdgeConfig(config);
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

	private boolean graphEdgeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteEdgeConfig(payload.getType(), payload.getFrom(), payload.getTo());
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

		if (domain.equalsIgnoreCase(Constants.ALL)) {
			return m_topologyConfigManager.insertDomainDefaultConfig(type, config);
		} else {
			return m_topologyConfigManager.insertDomainConfig(type, config);
		}
	}

	private boolean graphNodeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteDomainConfig(payload.getType(), payload.getDomain());
	}

	private boolean graphProductLineConfigAddOrUpdateSubmit(Payload payload, Model model) {
		ProductLine line = payload.getProductLine();
		String[] domains = payload.getDomains();

		return m_productLineConfigManger.insertProductLine(line, domains);
	}

	private void graphPruductLineAddOrUpdate(Payload payload, Model model) {
		String name = payload.getProductLineName();

		if (!StringUtil.isEmpty(name)) {
			model.setProductLine(m_productLineConfigManger.getCompany().findProductLine(name));
		}
	}

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
			model.setAggregationRules(m_aggreationConfigManager.queryAggrarationRules());
			break;
		case AGGREGATION_UPDATE:
			model.setAggregationRule(m_aggreationConfigManager.queryAggration(payload.getPattern()));
			break;
		case AGGREGATION_UPDATE_SUBMIT:
			updateAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggrarationRules());
			break;
		case AGGREGATION_DELETE:
			deleteAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggrarationRules());
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
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
			graphPruductLineAddOrUpdate(payload, model);
			model.setProjects(queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
			model.setOpState(m_productLineConfigManger.deleteProductLine(payload.getProductLineName()));
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphProductLineConfigAddOrUpdateSubmit(payload, model));
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			break;

		case METRIC_CONFIG_ADD_OR_UPDATE:
			metricConfigAdd(payload, model);
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());

			ProductLine productLine = m_productLineConfigManger.queryAllProductLines().get(payload.getProductLineName());
			if (productLine != null) {
				model.setProductLineToDomains(productLine.getDomains());
			}
			model.setProjects(queryAllProjects());
			break;
		case METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(metricConfigAddSubmit(payload, model));
			metricConfigList(payload, model);
			break;
		case METRIC_CONFIG_LIST:
			metricConfigList(payload, model);
			break;
		case METRIC_CONFIG_DELETE:
			model.setOpState(m_metricConfigManager.deleteDomainConfig(m_metricConfigManager.buildMetricKey(
			      payload.getDomain(), payload.getType(), payload.getMetricKey())));
			metricConfigList(payload, model);
			break;

		case EXCEPTION_THRESHOLDS:
			model.setExceptionLimits(m_exceptionConfigManager.queryAllExceptionLimits());
			break;
		case EXCEPTION_THRESHOLD_DELETE:
			deleteExceptionLimit(payload);
			model.setExceptionLimits(m_exceptionConfigManager.queryAllExceptionLimits());
			break;
		case EXCEPTION_THRESHOLD_UPDATE:
			model.setExceptionLimit(m_exceptionConfigManager.queryDomainExceptionLimit(payload.getDomain(),
			      payload.getException()));
			break;
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
			updateExceptionLimit(payload);
			model.setExceptionLimits(m_exceptionConfigManager.queryAllExceptionLimits());
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
		case UTILIZATION_CONFIG_UPDATE:
			String content = payload.getContent();
			if (!StringUtils.isEmpty(content)) {
				model.setOpState(m_utilizationConfigManager.insert(content));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_utilizationConfigManager.getUtilizationConfig().toString());
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
		case METRIC_GROUP_CONFIG_UPDATE:
			String metricGroupConfig = payload.getContent();
			if (!StringUtils.isEmpty(metricGroupConfig)) {
				model.setOpState(m_metricGroupConfigManager.insert(metricGroupConfig));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_metricGroupConfigManager.getMetricGroupConfig().toString());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void metricConfigAdd(Payload payload, Model model) {
		String key = m_metricConfigManager.buildMetricKey(payload.getDomain(), payload.getType(), payload.getMetricKey());

		model.setMetricItemConfig(m_metricConfigManager.queryMetricItemConfig(key));
	}

	private boolean metricConfigAddSubmit(Payload payload, Model model) {
		MetricItemConfig config = payload.getMetricItemConfig();
		String domain = config.getDomain();
		String type = config.getType();
		String metricKey = config.getMetricKey();

		if (!StringUtil.isEmpty(domain) && !StringUtil.isEmpty(type) && !StringUtil.isEmpty(metricKey)) {
			config.setId(m_metricConfigManager.buildMetricKey(domain, type, metricKey));
			return m_metricConfigManager.insertMetricItemConfig(config);
		} else {
			return false;
		}
	}

	private void metricConfigList(Payload payload, Model model) {
		Map<String, ProductLine> productLines = m_productLineConfigManger.queryAllProductLines();
		Map<ProductLine, List<MetricItemConfig>> metricConfigs = new LinkedHashMap<ProductLine, List<MetricItemConfig>>();
		Set<String> exists = new HashSet<String>();

		for (Entry<String, ProductLine> entry : productLines.entrySet()) {
			Set<String> domains = entry.getValue().getDomains().keySet();
			List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);

			for (MetricItemConfig config : configs) {
				exists.add(m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(), config.getMetricKey()));
			}

			metricConfigs.put(entry.getValue(), configs);
		}

		Map<String, MetricItemConfig> allConfigs = m_metricConfigManager.getMetricConfig().getMetricItemConfigs();
		Set<String> keysClone = new HashSet<String>(allConfigs.keySet());
		List<MetricItemConfig> otherConfigs = new ArrayList<MetricItemConfig>();

		for (String key : exists) {
			keysClone.remove(key);
		}
		for (String str : keysClone) {
			otherConfigs.add(allConfigs.get(str));
		}
		ProductLine otherProductLine = new ProductLine("Other").setTitle("Other");
		metricConfigs.put(otherProductLine, otherConfigs);
		model.setProductMetricConfigs(metricConfigs);
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

	private void updateAggregationRule(Payload payload) {
		AggregationRule proto = payload.getRule();
		m_aggreationConfigManager.insertAggregationRule(proto);
	}

	private void updateExceptionLimit(Payload payload) {
		ExceptionLimit limit = payload.getExceptionLimit();
		m_exceptionConfigManager.insertExceptionLimit(limit);

	}

	private void updateProject(Payload payload) {
		Project project = payload.getProject();
		project.setKeyId(project.getId());

		try {
			m_projectDao.updateByPK(project, ProjectEntity.UPDATESET_FULL);
			m_manager.getProjects().put(project.getDomain(), project);
		} catch (DalException e) {
			Cat.logError(e);
		}
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
