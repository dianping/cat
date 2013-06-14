package com.dianping.cat.report.page.dependency.graph;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public class TopologyGraphItemBuilder {

	@Inject
	private TopologyGraphConfigManager m_graphConfigManager;

	private static final int OK = GraphConstrant.OK;

	private static final String DATABASE = GraphConstrant.DATABASE;

	private static final String PROJECT = GraphConstrant.PROJECT;

	public TopologyNode createDatabaseNode(String database) {
		TopologyNode node = new TopologyNode(database);

		node.setStatus(OK);
		node.setType(DATABASE);
		node.setWeight(1);
		return node;
	}

	public TopologyEdge buildEdge(String domain, Dependency dependency) {
		TopologyEdge edge = new TopologyEdge();

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
		}
		return edge;
	}

	public TopologyNode createNode(String domain) {
		TopologyNode node = new TopologyNode(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		return node;
	}

	public TopologyNode buildNode(String domain, Index index) {
		TopologyNode node = new TopologyNode(domain);

		node.setType(PROJECT);
		node.setWeight(1);

		Pair<Integer, String> state = m_graphConfigManager.buildNodeState(domain, index);
		if (state != null) {
			node.setStatus(state.getKey());
			node.setDes(state.getValue());
		} else {
			node.setStatus(OK);
		}
		return node;
	}

}
