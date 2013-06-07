package com.dianping.cat.report.page.dependency.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.google.gson.Gson;

public class ProductLinesDashboard {

	private Map<String, List<Node>> productLines = new LinkedHashMap<String, List<Node>>();

	private List<Edge> edges = new ArrayList<Edge>();

	private transient Map<String, Node> temp = new LinkedHashMap<String, Node>();

	public String toJson() {
		String str = new Gson().toJson(this);
		str = str.replaceAll("\"m_", "\"");
		return str;
	}

	public boolean exsit(Node node) {
		return temp.containsKey(node.getId());
	}

	public ProductLinesDashboard addNode(String productLine, Node node) {
		List<Node> nodeList = productLines.get(productLine);

		if (nodeList == null) {
			nodeList = new ArrayList<Node>();
			productLines.put(productLine, nodeList);
		}

		nodeList.add(node);
		return this;
	}

	public ProductLinesDashboard addEdge(Edge edge) {
		edges.add(edge);
		return this;
	}

	public Map<String, List<Node>> getNodes() {
		return productLines;
	}

	public List<Edge> getEdges() {
		return edges;
	}

}
