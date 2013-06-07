package com.dianping.cat.report.page.dependency.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;

public class TopologyGraphBuilder extends BaseVisitor {

	private String m_domain;

	private TopologyGraphItemBuilder m_itemBuilder;

	private Map<Long, TopologyGraph> m_graphs = new HashMap<Long, TopologyGraph>(360);

	private int m_minute;

	private Date m_date;

	public Node createNode(String domain) {
		return m_itemBuilder.createNode(domain);
	}
	
	public Node cloneNode(Node node) {
		Node result = new Node();

		result.setDes(node.getDes());
		result.setId(node.getId());
		result.setLink(node.getLink());
		result.setStatus(node.getStatus());
		result.setType(node.getType());
		result.setWeight(node.getWeight());
		return result;
	}

	public Edge cloneEdge(Edge edge) {
		Edge result = new Edge();

		result.setDes(edge.getDes());
		result.setKey(edge.getKey());
		result.setLink(edge.getLink());
		result.setOpposite(edge.getOpposite());
		result.setSelf(edge.getSelf());
		result.setStatus(edge.getStatus());
		result.setTarget(edge.getTarget());
		result.setType(edge.getType());
		result.setWeight(edge.getWeight());
		return result;
	}

	private TopologyGraph findOrCreateGraph() {
		long time = m_date.getTime() + m_minute * TimeUtil.ONE_MINUTE;
		TopologyGraph graph = m_graphs.get(time);

		if (graph == null) {
			graph = new TopologyGraph();
			m_graphs.put(time, graph);
		}

		return graph;
	}

	public Map<Long, TopologyGraph> getGraphs() {
		return m_graphs;
	}

	public String mergeDes(String old, String des) {
		if (StringUtil.isEmpty(old)) {
			return des;
		} else if (StringUtil.isEmpty(des)) {
			return old;
		} else {
			return old + des;
		}
	}

	private Edge mergeEdge(Edge old, Edge edge) {
		if (old == null) {
			return edge;
		} else {
			if (edge.getStatus() > old.getStatus()) {
				old.setStatus(edge.getStatus());
			}
			if (edge.getWeight() > old.getWeight()) {
				old.setWeight(edge.getWeight());
			}
			old.setDes(mergeDes(old.getDes(), edge.getDes()));
			return old;
		}
	}

	private Node mergeNode(Node old, Node node) {
		if (old == null) {
			return node;
		} else {
			if (node.getStatus() > old.getStatus()) {
				old.setStatus(node.getStatus());
			}
			if (node.getWeight() > old.getWeight()) {
				old.setWeight(node.getWeight());
			}
			old.setDes(mergeDes(old.getDes(), node.getDes()));
			return old;
		}
	}

	public TopologyGraphBuilder setMinute(int minute) {
		m_minute = minute;
		return this;
	}

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();
		// pigeonServer is no use
		if (!"PigeonServer".equals(type)) {
			Edge edge = m_itemBuilder.buildEdge(m_domain, dependency);
			TopologyGraph graph = findOrCreateGraph();
			Edge old = graph.findEdge(edge.getKey());

			if (old != null) {
				System.out.println(old);
			}
			graph.getEdges().put(edge.getKey(), mergeEdge(old, edge));
			if ("Database".equals(type)) {
				String target = dependency.getTarget();
				Node nodeOld = graph.findNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, m_itemBuilder.createDatabaseNode(target)));
			}
		}
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		m_date = dependencyReport.getStartTime();
		m_domain = dependencyReport.getDomain();
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		TopologyGraph graph = findOrCreateGraph();
		Node node = m_itemBuilder.buildNode(m_domain, index);
		Node old = graph.findNode(node.getId());

		graph.getNodes().put(node.getId(), mergeNode(old, node));
	}

	@Override
	public void visitSegment(Segment segment) {
		m_minute = segment.getId();
		super.visitSegment(segment);
	}

}
