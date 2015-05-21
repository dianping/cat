package com.dianping.cat.system.page.config;

import java.util.List;

import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.config.entity.Tag;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Socket;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	private SystemPage m_page;

	@ObjectMeta("project")
	private Project m_project = new Project();

	@ObjectMeta("patternItem")
	private PatternItem m_patternItem = new PatternItem();

	@ObjectMeta("productLine")
	private ProductLine m_productLine = new ProductLine();

	@ObjectMeta("aggregation")
	private AggregationRule m_rule = new AggregationRule();

	@ObjectMeta("domainConfig")
	private DomainConfig m_domainConfig = new DomainConfig();

	@ObjectMeta("edgeConfig")
	private EdgeConfig m_edgeConfig = new EdgeConfig();

	@ObjectMeta("metricItemConfig")
	private MetricItemConfig m_metricItemConfig = new MetricItemConfig();

	@ObjectMeta("exceptionLimit")
	private ExceptionLimit m_exceptionLimit = new ExceptionLimit();

	@ObjectMeta("exceptionExclude")
	private ExceptionExclude m_exceptionExclude = new ExceptionExclude();

	@ObjectMeta("http")
	private Http m_http = new Http();

	@ObjectMeta("socket")
	private Socket m_socket = new Socket();

	@FieldMeta("pars")
	private String m_pars;

	@FieldMeta("projectId")
	private int m_projectId;

	@FieldMeta("productLineName")
	private String m_productLineName;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("domains")
	private String[] m_domains = new String[100];

	@FieldMeta("from")
	private String m_from;

	@FieldMeta("id")
	private int m_id;

	@FieldMeta("metricKey")
	private String m_metricKey;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("to")
	private String m_to;

	@FieldMeta("pattern")
	private String m_pattern;

	@FieldMeta("exception")
	private String m_exception;

	@FieldMeta(Constants.REPORT_BUG)
	private String m_bug;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("allOnOrOff")
	private String m_allOnOrOff;

	@FieldMeta("ruleId")
	private String m_ruleId;

	@FieldMeta("metrics")
	private String m_metrics;

	@FieldMeta("configs")
	private String m_configs;

	@FieldMeta("countTags")
	private String m_countTags;

	@FieldMeta("avgTags")
	private String m_avgTags;

	@FieldMeta("sumTags")
	private String m_sumTags;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("code")
	private int m_code;

	@FieldMeta("constant")
	private boolean m_constant = false;

	@FieldMeta("all")
	private boolean m_all = true;

	@FieldMeta("threshold")
	private int m_threshold = 30;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.TOPOLOGY_GRAPH_PRODUCT_LINE;
		}
		return m_action;
	}

	public String getAllOnOrOff() {
		return m_allOnOrOff;
	}

	public String getAvgTags() {
		return m_avgTags;
	}

	public String getBug() {
		return m_bug;
	}

	public int getCode() {
		return m_code;
	}

	public String getConfigs() {
		return m_configs;
	}

	public String getContent() {
		return m_content;
	}

	public String getCoungTags() {
		return m_countTags;
	}

	public String getDomain() {
		return m_domain;
	}

	public DomainConfig getDomainConfig() {
		return m_domainConfig;
	}

	public String[] getDomains() {
		return m_domains;
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public String getException() {
		return m_exception;
	}

	public ExceptionExclude getExceptionExclude() {
		return m_exceptionExclude;
	}

	public ExceptionLimit getExceptionLimit() {
		return m_exceptionLimit;
	}

	public String getFrom() {
		return m_from;
	}

	public Http getHttp() {
		return m_http;
	}

	public int getId() {
		return m_id;
	}

	public String getKey() {
		return m_key;
	}

	public MetricItemConfig getMetricItemConfig() {
		List<Tag> tags = m_metricItemConfig.getTags();

		if (!StringUtils.isEmpty(m_countTags)) {
			for (String tag : m_countTags.split(",")) {
				tag = tag.trim();
				if (!StringUtils.isEmpty(tag)) {
					Tag countTag = new Tag();

					countTag.setName(tag).setType("COUNT");
					tags.add(countTag);
				}
			}
		}

		if (!StringUtils.isEmpty(m_sumTags)) {
			for (String tag : m_sumTags.split(",")) {
				tag = tag.trim();
				if (!StringUtils.isEmpty(tag)) {
					Tag sumTag = new Tag();

					sumTag.setName(tag).setType("SUM");
					tags.add(sumTag);
				}
			}
		}

		if (!StringUtils.isEmpty(m_avgTags)) {
			for (String tag : m_avgTags.split(",")) {
				tag = tag.trim();
				if (!StringUtils.isEmpty(tag)) {
					Tag avgTag = new Tag();

					avgTag.setName(tag).setType("AVG");
					tags.add(avgTag);
				}
			}
		}

		return m_metricItemConfig;
	}

	public String getMetricKey() {
		if (m_metricKey != null) {
			m_metricKey = m_metricKey.trim();
		}
		return m_metricKey;
	}

	public String getMetrics() {
		return m_metrics;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getPars() {
		return m_pars;
	}

	public String getPattern() {
		return m_pattern;
	}

	public PatternItem getPatternItem() {
		return m_patternItem;
	}

	public ProductLine getProductLine() {
		return m_productLine;
	}

	public String getProductLineName() {
		return m_productLineName;
	}

	public Project getProject() {
		return m_project;
	}

	public int getProjectId() {
		return m_projectId;
	}

	public String getReportType() {
		return "";
	}

	public AggregationRule getRule() {
		return m_rule;
	}

	public String getRuleId() {
		return m_ruleId;
	}

	public Socket getSocket() {
		return m_socket;
	}

	public String getSumTags() {
		return m_sumTags;
	}

	public String getTitle() {
		return m_title;
	}

	public String getTo() {
		return m_to;
	}

	public String getType() {
		return m_type;
	}

	public boolean isAll() {
		return m_all;
	}

	public boolean isConstant() {
		return m_constant;
	}


	public void setAction(String action) {
		m_action = Action.getByName(action, Action.PROJECT_ALL);
	}

	public void setAll(boolean all) {
		m_all = all;
	}

	public void setAllOnOrOff(String allOnOrOff) {
		m_allOnOrOff = allOnOrOff;
	}

	public void setAvgTags(String avgTags) {
		m_avgTags = avgTags;
	}

	public void setBug(String bug) {
		m_bug = bug;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public void setConfigs(String configs) {
		m_configs = configs;
	}

	public void setConstant(boolean constant) {
		m_constant = constant;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setCoungTags(String coungTags) {
		m_countTags = coungTags;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setDomainConfig(DomainConfig domainConfig) {
		m_domainConfig = domainConfig;
	}

	public void setDomains(String[] domains) {
		m_domains = domains;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
	}

	public void setException(String exception) {
		m_exception = exception;
	}

	public void setExceptionLimit(ExceptionLimit exceptionLimit) {
		m_exceptionLimit = exceptionLimit;
	}

	public int getThreshold() {
		return m_threshold;
	}

	public void setThreshold(int threshold) {
		m_threshold = threshold;
	}

	public void setFrom(String from) {
		m_from = from;
	}

	public void setHttp(Http http) {
		m_http = http;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setKey(String key) {
		m_key = key;
	}

	public void setMetricItemConfig(MetricItemConfig metricItemConfig) {
		m_metricItemConfig = metricItemConfig;
	}

	public void setMetricKey(String metricKey) {
		m_metricKey = metricKey;
	}

	public void setMetrics(String metrics) {
		m_metrics = metrics;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.CONFIG);
	}

	public void setPars(String pars) {
		m_pars = pars;
	}

	public void setPattern(String pattern) {
		m_pattern = pattern;
	}

	public void setPatternItem(PatternItem patternItem) {
		m_patternItem = patternItem;
	}

	public void setProductLine(ProductLine productLine) {
		m_productLine = productLine;
	}

	public void setProductLineName(String productLineName) {
		m_productLineName = productLineName;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public void setProjectId(int id) {
		m_projectId = id;
	}

	public void setRule(AggregationRule rule) {
		m_rule = rule;
	}

	public void setRuleId(String ruleId) {
		m_ruleId = ruleId;
	}

	public void setSocket(Socket socket) {
		m_socket = socket;
	}

	public void setSumTags(String sumTags) {
		m_sumTags = sumTags;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setTo(String to) {
		m_to = to;
	}

	public void setType(String type) {
		if (type.startsWith("Cache.")) {
			type = "Cache";
		}
		if (type.equals("Call")) {
			type = "PigeonCall";
		}
		if (type.equals("Service")) {
			type = "PigeonService";
		}
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}

}
