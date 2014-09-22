package com.dianping.cat.system.page.config;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.codehaus.plexus.util.StringUtils;
import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.aggreation.model.entity.AggregationRule;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.RuleFTLDecorator;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.config.AlertConfigManager;
import com.dianping.cat.system.config.AlertPolicyManager;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.BusinessRuleConfigManager;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.config.MetricGroupConfigManager;
import com.dianping.cat.system.config.NetGraphConfigManager;
import com.dianping.cat.system.config.NetworkRuleConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.SystemRuleConfigManager;
import com.dianping.cat.system.config.ThirdPartyConfigManager;
import com.dianping.cat.system.config.WebRuleConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private TopologyGraphConfigManager m_topologyConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManger;

	@Inject
	private AggregationConfigManager m_aggreationConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject
	private DomainGroupConfigManager m_domainGroupConfigManger;

	@Inject
	private BugConfigManager m_bugConfigManager;

	@Inject
	private MetricGroupConfigManager m_metricGroupConfigManager;

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	private BusinessRuleConfigManager m_businessRuleConfigManager;

	@Inject
	private NetworkRuleConfigManager m_networkRuleConfigManager;

	@Inject
	private SystemRuleConfigManager m_systemRuleConfigManager;

	@Inject
	private WebRuleConfigManager m_webRuleConfigManager;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private AlertConfigManager m_alertConfigManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppComparisonConfigManager m_appComparisonConfigManager;

	@Inject
	private DomainNavManager m_manager;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	@Inject
	private AlertPolicyManager m_alertPolicyManager;

	@Inject
	private ThirdPartyConfigManager m_thirdPartyConfigManager;

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Inject
	private ConfigModificationService m_modificationService;

	@Inject
	private RuleFTLDecorator m_ruleDecorator;

	private void deleteAggregationRule(Payload payload) {
		m_aggreationConfigManager.deleteAggregationRule(payload.getPattern());
	}

	private void deleteExceptionExclude(Payload payload) {
		m_exceptionConfigManager.deleteExceptionExclude(payload.getDomain(), payload.getException());
	}

	private void deleteExceptionLimit(Payload payload) {
		m_exceptionConfigManager.deleteExceptionLimit(payload.getDomain(), payload.getException());
	}

	private void deleteProject(Payload payload) {
		Project proto = new Project();
		int id = payload.getProjectId();

		proto.setId(id);
		proto.setKeyId(id);
		m_projectService.deleteProject(proto);
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
	@PreInboundActionMeta("login")
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

		storeModifyInfo(ctx, payload);

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
		case PROJECT_DELETE:
			deleteProject(payload);
			model.setProjects(queryAllProjects());
			break;
		case AGGREGATION_ALL:
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case AGGREGATION_UPDATE:
			model.setAggregationRule(m_aggreationConfigManager.queryAggration(payload.getPattern()));
			break;
		case AGGREGATION_UPDATE_SUBMIT:
			updateAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case AGGREGATION_DELETE:
			deleteAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case URL_PATTERN_ALL:
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case URL_PATTERN_UPDATE:
			model.setPatternItem(m_urlPatternConfigManager.queryUrlPattern(payload.getKey()));
			break;
		case URL_PATTERN_UPDATE_SUBMIT:
			m_urlPatternConfigManager.insertPatternItem(payload.getPatternItem());
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case URL_PATTERN_DELETE:
			m_urlPatternConfigManager.deletePatternItem(payload.getKey());
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case WEB_RULE_UPDATE:
			String webRule = payload.getContent();
			if (!StringUtils.isEmpty(webRule)) {
				model.setOpState(m_webRuleConfigManager.insert(webRule));
			}
			model.setContent(m_webRuleConfigManager.getMonitorRules().toString());
			break;
		case APP_RULE_UPDATE:
			String appRule = payload.getContent();
			if (!StringUtils.isEmpty(appRule)) {
				model.setOpState(m_appRuleConfigManager.insert(appRule));
			}
			model.setContent(m_appRuleConfigManager.getMonitorRules().toString());
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
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
			graphPruductLineAddOrUpdate(payload, model);
			model.setProjects(queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
			model.setOpState(m_productLineConfigManger.deleteProductLine(payload.getProductLineName()));
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphProductLineConfigAddOrUpdateSubmit(payload, model));
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
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
		case METRIC_RULE_ADD_OR_UPDATE:
			metricRuleAdd(payload, model);
			break;
		case METRIC_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_businessRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
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
		case DOMAIN_METRIC_RULE_CONFIG_UPDATE:
			String domainMetricRuleConfig = payload.getContent();
			if (!StringUtils.isEmpty(domainMetricRuleConfig)) {
				model.setOpState(m_businessRuleConfigManager.insert(domainMetricRuleConfig));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_businessRuleConfigManager.getMonitorRules().toString());
			break;
		case NETWORK_RULE_CONFIG_LIST:
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_ADD_OR_UPDATE:
			generateRuleEditContent(payload.getKey(), "?op=networkRuleSubmit", "rule_network_metricItems.ftl",
			      "rule_configs.ftl", m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_networkRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_DELETE:
			model.setOpState(deleteRule(m_networkRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case SYSTEM_RULE_CONFIG_LIST:
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_ADD_OR_UPDATE:
			generateRuleEditContent(payload.getKey(), "?op=systemRuleSubmit", "rule_metricItems.ftl", "rule_configs.ftl",
			      m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_systemRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_DELETE:
			model.setOpState(deleteRule(m_systemRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;
		case ALERT_DEFAULT_RECEIVERS:
			String alertDefaultReceivers = payload.getContent();
			String allOnOrOff = payload.getAllOnOrOff();
			String xmlContent = m_alertConfigManager.buildReceiverContentByOnOff(alertDefaultReceivers, allOnOrOff);

			if (!StringUtils.isEmpty(alertDefaultReceivers)) {
				model.setOpState(m_alertConfigManager.insert(xmlContent));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_alertConfigManager.getAlertConfig().toString());
			break;
		case ALERT_POLICY:
			String alertPolicy = payload.getContent();

			if (!StringUtils.isEmpty(alertPolicy)) {
				model.setOpState(m_alertPolicyManager.insert(alertPolicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_alertPolicyManager.getAlertPolicy().toString());
			break;
		case EXCEPTION:
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_DELETE:
			deleteExceptionLimit(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_UPDATE:
			model.setExceptionLimit(m_exceptionConfigManager.queryDomainExceptionLimit(payload.getDomain(),
			      payload.getException()));
			break;
		case EXCEPTION_THRESHOLD_ADD:
			List<String> exceptionThresholdList = queryExceptionList();

			exceptionThresholdList.add(ExceptionConfigManager.TOTAL_STRING);
			model.setExceptionList(exceptionThresholdList);
			model.setDomainList(queryDoaminList());
			break;
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
			updateExceptionLimit(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_EXCLUDE_DELETE:
			deleteExceptionExclude(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_EXCLUDE_UPDATE:
			model.setExceptionExclude(m_exceptionConfigManager.queryDomainExceptionExclude(payload.getDomain(),
			      payload.getException()));
			break;
		case EXCEPTION_EXCLUDE_ADD:
			List<String> exceptionExcludeList = queryExceptionList();

			model.setExceptionList(exceptionExcludeList);
			model.setDomainList(queryDoaminList());
			break;
		case EXCEPTION_EXCLUDE_UPDATE_SUBMIT:
			updateExceptionExclude(payload);
			loadExceptionConfig(model);
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
		case NET_GRAPH_CONFIG_UPDATE:
			String netGraphConfig = payload.getContent();
			if (!StringUtils.isEmpty(netGraphConfig)) {
				model.setOpState(m_netGraphConfigManager.insert(netGraphConfig));
			}
			model.setContent(m_netGraphConfigManager.getConfig().toString());
			break;
		case APP_CONFIG_UPDATE:
			String appConfig = payload.getContent();
			if (!StringUtils.isEmpty(appConfig)) {
				model.setOpState(m_appConfigManager.insert(appConfig));
			}
			model.setContent(m_appConfigManager.getConfig().toString());
			break;
		case APP_COMPARISON_CONFIG_UPDATE:
			String appComparisonConfig = payload.getContent();
			if (!StringUtils.isEmpty(appComparisonConfig)) {
				model.setOpState(m_appComparisonConfigManager.insert(appComparisonConfig));
			}
			model.setContent(m_appComparisonConfigManager.getConfig().toString());
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
		}
		m_jspViewer.view(ctx, model);
	}

	private void generateRuleEditContent(String key, String href, String metricsTemplate, String configsTemplate,
	      BaseRuleConfigManager manager, Model model) {
		String metricsStr = "";
		String configsStr = "";
		String ruleId = "";

		if (!StringUtil.isEmpty(key)) {
			Rule rule = manager.queryRule(key);
			ruleId = rule.getId();
			metricsStr = new DefaultJsonBuilder().buildArray(rule.getMetricItems());
			configsStr = new DefaultJsonBuilder().buildArray(rule.getConfigs());
		}
		String metricsContent = m_ruleDecorator.generateMetricItemsHtml(metricsStr, metricsTemplate);
		String configsContent = m_ruleDecorator.generateConfigsHtml(href, configsStr, configsTemplate);

		model.setId(ruleId);
		model.setContent(metricsContent + configsContent);
	}

	private void generateRuleItemList(BaseRuleConfigManager manager, Model model) {
		Collection<Rule> rules = manager.getMonitorRules().getRules().values();
		List<RuleItem> ruleItems = new ArrayList<RuleItem>();

		for (Rule rule : rules) {
			String id = rule.getId();
			List<MetricItem> items = rule.getMetricItems();

			if (items.size() > 0) {
				MetricItem item = items.get(0);
				String productText = item.getProductText();
				String metricText = item.getMetricItemText();
				RuleItem ruleItem = new RuleItem(id, productText, metricText);

				ruleItem.setMonitorCount(item.isMonitorCount());
				ruleItem.setMonitorAvg(item.isMonitorAvg());
				ruleItem.setMonitorSum(item.isMonitorSum());

				ruleItems.add(ruleItem);
			}
		}
		model.setRuleItems(ruleItems);
	}

	private void loadExceptionConfig(Model model) {
		model.setExceptionExcludes(m_exceptionConfigManager.queryAllExceptionExcludes());
		model.setExceptionLimits(m_exceptionConfigManager.queryAllExceptionLimits());
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
			ProductLine productLine = entry.getValue();

			if (productLine.isMetricDashboard()) {
				Set<String> domains = productLine.getDomains().keySet();
				List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);

				for (MetricItemConfig config : configs) {
					exists.add(m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(),
					      config.getMetricKey()));
				}
				metricConfigs.put(productLine, configs);
			}
		}

		model.setProductMetricConfigs(metricConfigs);
	}

	private void metricRuleAdd(Payload payload, Model model) {
		String href = "?op=metricRuleAddSubmit";
		String key = m_metricConfigManager.buildMetricKey(payload.getDomain(), payload.getType(), payload.getMetricKey());
		Rule rule = m_businessRuleConfigManager.queryRule(payload.getProductLineName(), key);
		String metricsStr = new DefaultJsonBuilder().buildArray(rule.getMetricItems());
		String metricsContent = m_ruleDecorator.generateMetricItemsHtml(metricsStr, "rule_business_metricItems.ftl");
		String configsStr = new DefaultJsonBuilder().buildArray(rule.getConfigs());
		String configsContent = m_ruleDecorator.generateConfigsHtml(href, configsStr, "rule_configs.ftl");

		model.setId(rule.getId());
		model.setContent(metricsContent + configsContent);
	}

	private boolean addSubmitRule(BaseRuleConfigManager manager, String id, String metrics, String configs) {
		try {
			String xmlContent = manager.updateRule(id, metrics, configs);
			return manager.insert(xmlContent);
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean deleteRule(BaseRuleConfigManager manager, String key) {
		try {
			String xmlContent = manager.deleteRule(key);
			return manager.insert(xmlContent);
		} catch (Exception ex) {
			return false;
		}
	}

	private List<Project> queryAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		try {
			projects = m_projectService.findAll();
		} catch (Exception e) {
			Cat.logError(e);
		}
		Collections.sort(projects, new ProjectCompartor());
		return projects;
	}

	private List<String> queryDoaminList() {
		List<String> result = new ArrayList<String>();
		List<Project> projects = queryAllProjects();

		result.add("Default");
		for (Project p : projects) {
			result.add(p.getDomain());
		}
		return result;
	}

	private List<String> queryExceptionList() {
		long current = System.currentTimeMillis();
		Date start = new Date(current - current % TimeUtil.ONE_HOUR - TimeUtil.ONE_HOUR - TimeUtil.ONE_DAY);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		BugReport report = m_reportService.queryBugReport(Constants.CAT, start, end);
		Set<String> keys = new HashSet<String>();
		List<String> exceptions = new ArrayList<String>();

		for (Entry<String, com.dianping.cat.home.bug.entity.Domain> domain : report.getDomains().entrySet()) {
			keys.addAll(domain.getValue().getExceptionItems().keySet());
		}

		for (String key : keys) {
			exceptions.add(key.replaceAll("\n", " "));
		}
		return exceptions;
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

	private void storeModifyInfo(Context ctx, Payload payload) {
		Cookie cookie = ctx.getCookie("ct");

		if (cookie != null) {
			String cookieValue = cookie.getValue();

			try {
				String[] values = cookieValue.split("\\|");
				String userName = values[0];
				String account = values[1];

				if (userName.startsWith("\"")) {
					userName = userName.substring(1, userName.length() - 1);
				}
				userName = URLDecoder.decode(userName, "UTF-8");

				m_modificationService.store(userName, account, payload);
			} catch (Exception ex) {
				Cat.logError("store cookie fail:" + cookieValue, new RuntimeException());
			}
		} else {
			Cat.logError("cannot get cookie info", new RuntimeException());
		}
	}

	private void updateAggregationRule(Payload payload) {
		AggregationRule proto = payload.getRule();
		m_aggreationConfigManager.insertAggregationRule(proto);
	}

	private void updateExceptionExclude(Payload payload) {
		ExceptionExclude exclude = payload.getExceptionExclude();
		String domain = payload.getDomain();
		String exception = payload.getException();

		if (domain != null && exception != null) {
			m_exceptionConfigManager.deleteExceptionExclude(domain, exception);
		}
		m_exceptionConfigManager.insertExceptionExclude(exclude);
	}

	private void updateExceptionLimit(Payload payload) {
		ExceptionLimit limit = payload.getExceptionLimit();
		m_exceptionConfigManager.insertExceptionLimit(limit);
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

	public class RuleItem {
		private String m_id;

		private String m_productlineText;

		private String m_metricText;

		private boolean m_monitorCount;

		private boolean m_monitorSum;

		private boolean m_monitorAvg;

		public RuleItem(String id, String productlineText, String metricText) {
			m_id = id;
			m_productlineText = productlineText;
			m_metricText = metricText;
		}

		public String getId() {
			return m_id;
		}

		public String getMetricText() {
			return m_metricText;
		}

		public String getProductlineText() {
			return m_productlineText;
		}

		public boolean isMonitorAvg() {
			return m_monitorAvg;
		}

		public boolean isMonitorCount() {
			return m_monitorCount;
		}

		public boolean isMonitorSum() {
			return m_monitorSum;
		}

		public void setId(String id) {
			m_id = id;
		}

		public void setMetricText(String metricText) {
			m_metricText = metricText;
		}

		public void setMonitorAvg(boolean monitorAvg) {
			m_monitorAvg = monitorAvg;
		}

		public void setMonitorCount(boolean monitorCount) {
			m_monitorCount = monitorCount;
		}

		public void setMonitorSum(boolean monitorSum) {
			m_monitorSum = monitorSum;
		}

		public void setProductlineText(String productlineText) {
			m_productlineText = productlineText;
		}
	}

}
