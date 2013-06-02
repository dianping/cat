package com.dianping.cat.report.page.dependency.graph;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;

public class TopologyGraphBuilder extends BaseVisitor {

	private TopologyGraph m_currentGraph;

	private String m_domain;

	private boolean m_isCurrent;

	private TopologyGraphItemBuilder m_itemBuilder;

	private TopologyGraph m_lastGraph;

	private int m_minute;

	public Node createNode(String domain) {
		return m_itemBuilder.createNode(domain);
	}

	private TopologyGraph getGraph() {
		if (m_isCurrent) {
			return m_currentGraph;
		} else {
			return m_lastGraph;
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
			if (!StringUtil.isEmpty(edge.getLink())) {
				old.setLink(edge.getLink());
			}
			return old;
		}
	}

	public String mergeDes(String old, String des) {
		String split = "\\|";
		if (StringUtil.isEmpty(old)) {
			return des;
		} else if(StringUtil.isEmpty(des)){
			return old;
		}else {
			String[] temps = old.split(split);
			for (String temp : temps) {
				if (des.equals(temp.trim())) {
					return old;
				}
			}
		}
		return old + split + des;
	}

	private Node mergeNode(Node old, Node node) {
		if (old == null) {
			return node;
		} else {
			if (node.getStatus() > old.getStatus()) {
				old.setStatus(node.getStatus());
			}
			if (node.getWeight() < old.getWeight()) {
				old.setWeight(node.getWeight());
			}
			old.setDes(mergeDes(old.getDes(), node.getDes()));
			if (!StringUtil.isEmpty(node.getLink())) {
				old.setLink(node.getLink());
			}
			return old;
		}
	}

	public TopologyGraphBuilder setCurrentGraph(TopologyGraph graph) {
		m_currentGraph = graph;
		return this;
	}

	public TopologyGraphBuilder setLastGraph(TopologyGraph graph) {
		m_lastGraph = graph;
		return this;
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
			TopologyGraph graph = getGraph();
			Edge old = graph.findEdge(edge.getKey());

			graph.getEdges().put(edge.getKey(), mergeEdge(old, edge));
			if ("Database".equals(type)) {
				String target = dependency.getTarget();
				Node nodeOld = graph.findNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, m_itemBuilder.buildDatabaseNode(target)));
			}
		}
		super.visitDependency(dependency);
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		m_domain = dependencyReport.getDomain();
		m_itemBuilder.setDate(dependencyReport.getStartTime());
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		TopologyGraph graph = getGraph();
		Node node = m_itemBuilder.buildNode(m_domain, index);
		Node old = graph.findNode(node.getId());

		graph.getNodes().put(node.getId(), mergeNode(old, node));
		super.visitIndex(index);
	}

	@Override
	public void visitSegment(Segment segment) {
		int id = segment.getId();

		if (id == m_minute) {
			super.visitSegment(segment);
			m_isCurrent = true;
		} else if (id + 1 == m_minute) {
			super.visitSegment(segment);
			m_isCurrent = false;
		}

	}
}
