package com.dianping.cat.system.page.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.aggreation.model.entity.AggregationRule;
import com.dianping.cat.consumer.company.model.entity.Domain;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Project m_project;

	private List<Project> m_projects;

	private AggregationRule m_aggregationRule;

	private List<AggregationRule> m_aggregationRules;

	private ExceptionLimit m_exceptionLimit;

	private List<ExceptionLimit> m_exceptionLimits;

	private String m_opState = SUCCESS;

	private TopologyGraphConfig m_config;

	private Map<String, Edge> m_edgeConfigs = new HashMap<String, Edge>();

	private DomainConfig m_domainConfig;

	private EdgeConfig m_edgeConfig;

	private ProductLine m_productLine;

	private Map<String, ProductLine> m_productLines;

	private MetricItemConfig m_metricItemConfig;

	private Map<ProductLine, List<MetricItemConfig>> m_productMetricConfigs;
	
	private String m_bug;
	
	private String m_content;
	
	private Map<String, Domain> m_productLineToDomains;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	public Map<String, Domain> getProductLineToDomains() {
		return m_productLineToDomains;
	}

	public void setProductLineToDomains(Map<String, Domain> productLineToDomains) {
		m_productLineToDomains = productLineToDomains;
	}

	public Model(Context ctx) {
		super(ctx);
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

	public TopologyGraphConfig getConfig() {
		return m_config;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.PROJECT_ALL;
	}

	public String getDomain() {
		return "";
	}

	public DomainConfig getDomainConfig() {
		return m_domainConfig;
	}

	public List<String> getDomains() {
		return Collections.emptyList();
	}

	public EdgeConfig getEdgeConfig() {
		return m_edgeConfig;
	}

	public Map<String, Edge> getEdgeConfigs() {
		return m_edgeConfigs;
	}

	public String getIpAddress() {
		return "";
	}

	public String getOpState() {
		return m_opState;
	}

	public ProductLine getProductLine() {
		return m_productLine;
	}

	public Map<String, ProductLine> getProductLines() {
		return m_productLines;
	}

	public Project getProject() {
		return m_project;
	}

	public List<Project> getProjects() {
		return m_projects;
	}

	public void setAggregationRule(AggregationRule aggregationRule) {
		m_aggregationRule = aggregationRule;
	}

	public void setAggregationRules(List<AggregationRule> aggregationRules) {
		m_aggregationRules = aggregationRules;
	}

	public void setConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public void setDomainConfig(DomainConfig domainConfig) {
		m_domainConfig = domainConfig;
	}

	public void setEdgeConfig(EdgeConfig edgeConfig) {
		m_edgeConfig = edgeConfig;
	}

	public void setGraphConfig(TopologyGraphConfig config) {
		m_config = config;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setProductLine(ProductLine productLine) {
		m_productLine = productLine;
	}

	public void setProductLines(Map<String, ProductLine> productLines) {
		m_productLines = productLines;
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public void setProjects(List<Project> projects) {
		m_projects = projects;
	}

	public MetricItemConfig getMetricItemConfig() {
		return m_metricItemConfig;
	}

	public void setMetricItemConfig(MetricItemConfig metricItemConfig) {
		m_metricItemConfig = metricItemConfig;
	}

	public Map<ProductLine, List<MetricItemConfig>> getProductMetricConfigs() {
		return m_productMetricConfigs;
	}

	public void setProductMetricConfigs(Map<ProductLine, List<MetricItemConfig>> productMetricConfigs) {
		m_productMetricConfigs = productMetricConfigs;
	}

	public ExceptionLimit getExceptionLimit() {
		return m_exceptionLimit;
	}

	public List<ExceptionLimit> getExceptionLimits() {
		return m_exceptionLimits;
	}

	public void setExceptionLimit(ExceptionLimit exceptionLimit) {
		m_exceptionLimit = exceptionLimit;
	}

	public void setExceptionLimits(List<ExceptionLimit> exceptionLimits) {
		m_exceptionLimits = exceptionLimits;
	}

	public String getBug() {
   	return m_bug;
   }

	public void setBug(String bug) {
   	m_bug = bug;
   }
	
	public String getContent() {
   	return m_content;
   }

	public void setContent(String content) {
   	m_content = content;
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
