package com.dianping.cat.system.page.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.consumer.core.dal.Project;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.AggregationRule;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.ProductLine;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Project m_project;

	private List<Project> m_projects;

	private AggregationRule m_aggregationRule;

	private List<AggregationRule> m_aggregationRules;

	private String m_opState = CatString.SUCCESS;

	private TopologyGraphConfig m_config;

	private Map<String, Edge> m_edgeConfigs = new HashMap<String, Edge>();

	private DomainConfig m_domainConfig;

	private EdgeConfig m_edgeConfig;
	
	private ProductLine m_productLine;

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
			m_opState = CatString.SUCCESS;
		} else {
			m_opState = CatString.FAIL;
		}
	}

	public void setProject(Project project) {
		m_project = project;
	}

	public void setProjects(List<Project> projects) {
		m_projects = projects;
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

	public ProductLine getProductLine() {
   	return m_productLine;
   }

	public void setProductLine(ProductLine productLine) {
   	m_productLine = productLine;
   }
	
}
