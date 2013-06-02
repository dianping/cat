package com.dianping.cat.report.page.dependency.graph;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;

public class TopologyGraphItemBuilder {

	@Inject
	private TopologyGraphConfigManger m_graphConfigManager;

	private Date m_start;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private static final int OK = GraphConstrant.OK;

	private static final String DATABASE = GraphConstrant.DATABASE;

	private static final String PROJECT = GraphConstrant.PROJECT;

	public Node buildDatabaseNode(String database) {
		Node node = new Node(database);

		node.setStatus(OK);
		node.setType(DATABASE);
		node.setWeight(1);
		node.setDes(database);
		node.setLink("");
		return node;
	}

	public Edge buildEdge(String domain, Dependency dependency) {
		Edge edge = new Edge();

		edge.setType(dependency.getType());
		edge.setKey(dependency.getType() + ':' + domain + ':' + dependency.getTarget());
		edge.setSelf(domain);
		edge.setTarget(dependency.getTarget());
		edge.setOpposite(false);
		edge.setWeight(1);

		Pair<Integer, String> state = m_graphConfigManager.buildEdgeState(domain, dependency);
		if (state != null) {
			edge.setStatus(state.getKey());
			edge.setDes(state.getValue());
		} else {
			edge.setStatus(OK);
			edge.setDes("");
		}
		edge.setLink(buildProblemLink(domain, m_start));
		return edge;
	}

	public Node createNode(String domain) {
		Node node = new Node(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		node.setDes("");
		node.setLink(buildProblemLink(domain, m_start));
		return node;
	}

	public Node buildNode(String domain, Index index) {
		Node node = new Node(domain);

		node.setType(PROJECT);
		node.setWeight(1);
		node.setLink(buildProblemLink(domain, m_start));

		Pair<Integer, String> state = m_graphConfigManager.buildNodeState(domain, index);
		if (state != null) {
			node.setStatus(state.getKey());
			node.setDes(state.getValue());
		} else {
			node.setStatus(OK);
			node.setDes("");
		}
		return node;
	}

	private String buildProblemLink(String domain, Date date) {
		return "p?domain=" + domain + "&date=" + m_sdf.format(date);
	}

	public TopologyGraphItemBuilder setDate(Date start) {
		m_start = start;
		return this;
	}

}
