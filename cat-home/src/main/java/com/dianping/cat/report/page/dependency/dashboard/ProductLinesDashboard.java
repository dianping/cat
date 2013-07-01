package com.dianping.cat.report.page.dependency.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.google.gson.Gson;

public class ProductLinesDashboard {

	private Map<String, List<TopologyNode>> productLines = new LinkedHashMap<String, List<TopologyNode>>();

	private List<TopologyEdge> edges = new ArrayList<TopologyEdge>();

	private transient Map<String, TopologyNode> temp = new LinkedHashMap<String, TopologyNode>();

	public String toJson() {
		String str = new Gson().toJson(this);
		str = str.replaceAll("\"m_", "\"");
		
		return str;
	}

	public boolean exsit(TopologyNode node) {
		return temp.containsKey(node.getId());
	}

	public ProductLinesDashboard addNode(String productLine, TopologyNode node) {
		List<TopologyNode> nodeList = productLines.get(productLine);

		if (nodeList == null) {
			nodeList = new ArrayList<TopologyNode>();
			productLines.put(productLine, nodeList);
		}

		nodeList.add(node);
		return this;
	}

	public ProductLinesDashboard addEdge(TopologyEdge edge) {
		edges.add(edge);
		return this;
	}

	public Map<String, List<TopologyNode>> getNodes() {
		return productLines;
	}

	public List<TopologyEdge> getEdges() {
		return edges;
	}

}
