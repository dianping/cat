package com.dianping.cat.system.page.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.ConfigItem;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.company.model.entity.Domain;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Socket;
import com.dianping.cat.home.alert.thirdparty.entity.ThirdPartyConfig;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.report.page.web.CityManager.City;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.processor.BaseProcesser.RuleItem;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Project m_project;

	private List<Project> m_projects;

	private AggregationRule m_aggregationRule;

	private List<AggregationRule> m_aggregationRules;

	private PatternItem m_patternItem;

	private Map<Integer, PatternItem> m_patternItems;

	private ExceptionLimit m_exceptionLimit;

	private List<ExceptionLimit> m_exceptionLimits;

	private ExceptionExclude m_exceptionExclude;

	private List<ExceptionExclude> m_exceptionExcludes;

	private String m_opState = SUCCESS;

	private TopologyGraphConfig m_config;

	private Map<String, Edge> m_edgeConfigs = new HashMap<String, Edge>();

	private DomainConfig m_domainConfig;

	private EdgeConfig m_edgeConfig;

	private ProductLine m_productLine;

	private Map<String, ProductLine> m_productLines;

	private Map<String, List<ProductLine>> m_typeToProductLines;

	private MetricItemConfig m_metricItemConfig;

	private Map<ProductLine, List<MetricItemConfig>> m_productMetricConfigs;

	private String m_bug;

	private String m_content;

	private String m_metricItemConfigRule;

	private Map<String, Domain> m_productLineToDomains;

	private List<String> m_domainList;

	private List<String> m_exceptionList;

	private List<RuleItem> m_ruleItems;

	private Collection<Rule> m_rules;

	private String m_id;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_versions;

	private Map<Integer, Item> m_connectionTypes;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_platforms;

	private List<Command> m_commands;

	private Map<String, List<City>> m_citiyInfos;

	private String m_duplicateDomains;

	private List<String> m_tags;

	private String m_configHeader;

	private Command m_updateCommand;

	private Map<Integer, Code> m_codes;

	private Map<Integer, com.dianping.cat.configuration.web.url.entity.Code> m_webCodes;

	private Code m_code;

	private String m_domain;

	private Map<Integer, Speed> m_speeds;

	private Speed m_speed;

	private String m_nameUniqueResult;

	private ThirdPartyConfig m_thirdPartyConfig;

	private List<String> m_heartbeatExtensionMetrics;

	private Http m_http;

	private Socket m_socket;

	private DomainGroup m_domainGroup;

	private com.dianping.cat.home.group.entity.Domain m_groupDomain;

	private List<String> m_validatePaths;

	private List<String> m_invalidatePaths;

	private AppConfigManager m_appConfigManager;

	private Item m_appItem;

	public Model(Context ctx) {
		super(ctx);
		try {
			m_appConfigManager = ContainerLoader.getDefaultContainer().lookup(AppConfigManager.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void buildEdgeInfo() {
		Map<String, EdgeConfig> edges = m_config.getEdgeConfigs();

		for (EdgeConfig edge : edges.values()) {
			String type = edge.getType();
			Edge temp = m_edgeConfigs.get(type);

			if (temp == null) {
				List<EdgeConfig> edgeConfigs = new ArrayList<EdgeConfig>();
				temp = new Edge(edgeConfigs, m_config.findNodeConfig(edge.getType()));
				m_edgeConfigs.put(type, temp);
			}
			temp.getEdgeConfigs().add(edge);
		}
	}

	public AggregationRule getAggregationRule() {
		return m_aggregationRule;
	}

	public List<AggregationRule> getAggregationRules() {
		return m_aggregationRules;
	}

	public Map<String, List<Command>> getApiCommands() {
		return m_appConfigManager.queryDomain2Commands();
	}

	public Item getAppItem() {
		return m_appItem;
	}

	public String getBug() {
		return m_bug;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public String getCityInfo() {
		return new JsonBuilder().toJson(m_citiyInfos);
	}

	public Map<String, List<City>> getCityInfos() {
		return m_citiyInfos;
	}

	public Code getCode() {
		return m_code;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	public String getCommandJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryCommand2Codes());
	}

	public List<Command> getCommands() {
		return m_commands;
	}

	public TopologyGraphConfig getConfig() {
		return m_config;
	}

	public String getConfigHeader() {
		return m_configHeader;
	}

	public Map<String, ConfigItem> getConfigItems() {
		return m_appConfigManager.getConfig().getConfigItems();
	}

	public Map<Integer, Item> getConnectionTypes() {
		return m_connectionTypes;
	}

	public String getContent() {
		return m_content;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.PROJECT_ALL;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getDomain2CommandsJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryDomain2Commands());
	}

	public DomainConfig getDomainConfig() {
		return m_domainConfig;
	}

	public DomainGroup getDomainGroup() {
		return m_domainGroup;
	}

	public List<String> getDomainList() {
		return m_domainList;
	}

	public List<String> getDomains() {
		return Collections.emptyList();
	}

	public String getDuplicateDomains() {
		return m_duplicateDomains;
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public Map<String, Edge> getEdgeConfigs() {
		return m_edgeConfigs;
	}

	public ExceptionExclude getExceptionExclude() {
		return m_exceptionExclude;
	}

	public List<ExceptionExclude> getExceptionExcludes() {
		return m_exceptionExcludes;
	}

	public ExceptionLimit getExceptionLimit() {
		return m_exceptionLimit;
	}

	public List<ExceptionLimit> getExceptionLimits() {
		return m_exceptionLimits;
	}

	public List<String> getExceptionList() {
		return m_exceptionList;
	}

	public com.dianping.cat.home.group.entity.Domain getGroupDomain() {
		return m_groupDomain;
	}

	public List<String> getHeartbeatExtensionMetrics() {
		return m_heartbeatExtensionMetrics;
	}

	public Http getHttp() {
		return m_http;
	}

	public String getId() {
		return m_id;
	}

	public List<String> getInvalidatePaths() {
		return m_invalidatePaths;
	}

	public String getIpAddress() {
		return "";
	}

	public MetricItemConfig getMetricItemConfig() {
		return m_metricItemConfig;
	}

	public String getMetricItemConfigRule() {
		return m_metricItemConfigRule;
	}

	public String getNameUniqueResult() {
		return m_nameUniqueResult;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public String getOpState() {
		return m_opState;
	}

	public PatternItem getPatternItem() {
		return m_patternItem;
	}

	public Map<Integer, PatternItem> getPatternItems() {
		return m_patternItems;
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public ProductLine getProductLine() {
		return m_productLine;
	}

	public Map<String, ProductLine> getProductLines() {
		return m_productLines;
	}

	public Map<String, Domain> getProductLineToDomains() {
		return m_productLineToDomains;
	}

	public Map<ProductLine, List<MetricItemConfig>> getProductMetricConfigs() {
		return m_productMetricConfigs;
	}

	public Project getProject() {
		return m_project;
	}

	public List<Project> getProjects() {
		return m_projects;
	}

	public String getReportType() {
		return "";
	}

	public List<RuleItem> getRuleItems() {
		return m_ruleItems;
	}

	public Collection<Rule> getRules() {
		return m_rules;
	}

	public Socket getSocket() {
		return m_socket;
	}

	public Speed getSpeed() {
		return m_speed;
	}

	public Map<Integer, Speed> getSpeeds() {
		return m_speeds;
	}

	public List<String> getTags() {
		return m_tags;
	}

	public ThirdPartyConfig getThirdPartyConfig() {
		return m_thirdPartyConfig;
	}

	public Map<String, List<ProductLine>> getTypeToProductLines() {
		return m_typeToProductLines;
	}

	public Command getUpdateCommand() {
		return m_updateCommand;
	}

	public List<String> getValidatePaths() {
		return m_validatePaths;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public Map<Integer, com.dianping.cat.configuration.web.url.entity.Code> getWebCodes() {
		return m_webCodes;
	}

	public String getWebCodesJson() {
		return new JsonBuilder().toJson(m_webCodes);
	}

	public void setAggregationRule(AggregationRule aggregationRule) {
		m_aggregationRule = aggregationRule;
	}

	public void setAggregationRules(List<AggregationRule> aggregationRules) {
		m_aggregationRules = aggregationRules;
	}

	public void setAppItem(Item appItem) {
		m_appItem = appItem;
	}

	public void setBug(String bug) {
		m_bug = bug;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCityInfos(Map<String, List<City>> cityInfos) {
		m_citiyInfos = cityInfos;
	}

	public void setCode(Code code) {
		m_code = code;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setCommands(List<Command> commands) {
		m_commands = commands;
	}

	public void setConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public void setConfigHeader(String configHeader) {
		m_configHeader = configHeader;
	}

	public void setConnectionTypes(Map<Integer, Item> connectionTypes) {
		m_connectionTypes = connectionTypes;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setDomainConfig(DomainConfig domainConfig) {
		m_domainConfig = domainConfig;
	}

	public void setDomainGroup(DomainGroup domainGroup) {
		m_domainGroup = domainGroup;
	}

	public void setDomainList(List<String> domainList) {
		m_domainList = domainList;
	}

	public void setDuplicateDomains(String duplicateDomains) {
		m_duplicateDomains = duplicateDomains;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
	}

	public void setExceptionExclude(ExceptionExclude exceptionExclude) {
		m_exceptionExclude = exceptionExclude;
	}

	public void setExceptionExcludes(List<ExceptionExclude> exceptionExcludes) {
		m_exceptionExcludes = exceptionExcludes;
	}

	public void setExceptionLimit(ExceptionLimit exceptionLimit) {
		m_exceptionLimit = exceptionLimit;
	}

	public void setExceptionLimits(List<ExceptionLimit> exceptionLimits) {
		m_exceptionLimits = exceptionLimits;
	}

	public void setExceptionList(List<String> exceptionList) {
		m_exceptionList = exceptionList;
	}

	public void setGraphConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public void setGroupDomain(com.dianping.cat.home.group.entity.Domain groupDomain) {
		m_groupDomain = groupDomain;
	}

	public void setHeartbeatExtensionMetrics(List<String> heartbeatExtensionMetrics) {
		m_heartbeatExtensionMetrics = heartbeatExtensionMetrics;
	}

	public void setHttp(Http http) {
		m_http = http;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setInvalidatePaths(List<String> invalidatePaths) {
		m_invalidatePaths = invalidatePaths;
	}

	public void setMetricItemConfig(MetricItemConfig metricItemConfig) {
		m_metricItemConfig = metricItemConfig;
	}

	public void setMetricItemConfigRule(String metricItemConfigRule) {
		m_metricItemConfigRule = metricItemConfigRule;
	}

	public void setNameUniqueResult(String nameUniqueResult) {
		m_nameUniqueResult = nameUniqueResult;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setPatternItem(PatternItem patternItem) {
		m_patternItem = patternItem;
	}

	public void setPatternItems(Map<Integer, PatternItem> patternItems) {
		m_patternItems = patternItems;
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setProductLine(ProductLine productLine) {
		m_productLine = productLine;
	}

	public void setProductLines(Map<String, ProductLine> productLines) {
		m_productLines = productLines;
	}

	public void setProductLineToDomains(Map<String, Domain> productLineToDomains) {
		m_productLineToDomains = productLineToDomains;
	}

	public void setProductMetricConfigs(Map<ProductLine, List<MetricItemConfig>> productMetricConfigs) {
		m_productMetricConfigs = productMetricConfigs;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public void setProjects(List<Project> projects) {
		m_projects = projects;
	}

	public void setRuleItems(List<RuleItem> ruleItems) {
		m_ruleItems = ruleItems;
	}

	public void setRules(Collection<Rule> rules) {
		m_rules = rules;
	}

	public void setSocket(Socket socket) {
		m_socket = socket;
	}

	public void setSpeed(Speed speed) {
		m_speed = speed;
	}

	public void setSpeeds(Map<Integer, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setTags(List<String> tags) {
		m_tags = tags;
	}

	public void setThirdPartyConfig(ThirdPartyConfig thirdPartyConfig) {
		m_thirdPartyConfig = thirdPartyConfig;
	}

	public void setTypeToProductLines(Map<String, List<ProductLine>> typeToProductLines) {
		m_typeToProductLines = typeToProductLines;
	}

	public void setUpdateCommand(Command updateCommand) {
		m_updateCommand = updateCommand;
	}

	public void setValidatePaths(List<String> validatePaths) {
		m_validatePaths = validatePaths;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}

	public void setWebCodes(Map<Integer, com.dianping.cat.configuration.web.url.entity.Code> webCodes) {
		m_webCodes = webCodes;
	}

	public static class Edge {
		private List<EdgeConfig> m_edgeConfigs;

		private NodeConfig m_nodeConfig;

		public Edge(List<EdgeConfig> edgeConfigs, NodeConfig nodeConfig) {
			m_edgeConfigs = edgeConfigs;
			m_nodeConfig = nodeConfig;
		}

		public List<EdgeConfig> getEdgeConfigs() {
			return m_edgeConfigs;
		}

		public NodeConfig getNodeConfig() {
			return m_nodeConfig;
		}
	}
}
