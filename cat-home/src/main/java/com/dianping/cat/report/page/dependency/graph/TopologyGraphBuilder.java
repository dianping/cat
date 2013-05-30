package com.dianping.cat.report.page.dependency.graph;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.home.dependency.entity.DependencyGraph;
import com.dianping.cat.home.dependency.entity.Edge;
import com.dianping.cat.home.dependency.entity.Node;

public class TopologyGraphBuilder extends BaseVisitor {

	private String m_domain;

	private DependencyGraph m_graph;

	private int m_minute;

	private TopologyGraphItemBuilder m_itemBuilder;

	private Node mergeNode(Node old, Node node) {
		if (old == null) {
			return node;
		} else {
			if (old.getStatus() > old.getStatus()) {
				old.setStatus(old.getStatus());
			}
			old.setWeight(old.getWeight());
			old.setDes(old.getDes() + node.getDes());
			if (!StringUtil.isEmpty(node.getLink())) {
				old.setLink(node.getLink());
			}
			return old;
		}
	}

	private Edge mergeEdge(Edge old, Edge edge) {
		if (old == null) {
			return edge;
		} else {
			if (old.getStatus() > old.getStatus()) {
				old.setStatus(old.getStatus());
			}
			old.setWeight(old.getWeight() + edge.getWeight());
			old.setDes(old.getDes() + edge.getDes());
			if (!StringUtil.isEmpty(edge.getLink())) {
				old.setLink(edge.getLink());
			}
			return old;
		}
	}

	public TopologyGraphBuilder setGraph(DependencyGraph graph) {
		m_graph = graph;
		return this;
	}

	public TopologyGraphBuilder setMinute(int minute) {
		m_minute = minute;
		return this;
	}

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();

		//pigeonServer is no use
		if (!"PigeonServer".equals(type)) {
			Edge edge = m_itemBuilder.buildEdge(m_domain, dependency);
			Edge old = m_graph.findEdge(edge.getKey());

			m_graph.getEdges().put(edge.getKey(), mergeEdge(old, edge));
			if ("Database".equals(type)) {
				String target = dependency.getTarget();
				Node nodeOld = m_graph.findNode(target);

				m_graph.getNodes().put(target, mergeNode(nodeOld, m_itemBuilder.buildDatabaseNode(target)));
			}
		}
		super.visitDependency(dependency);
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		m_domain = dependencyReport.getDomain();
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		Node node = m_itemBuilder.buildNode(m_domain, index);
		Node old = m_graph.findNode(node.getId());

		m_graph.getNodes().put(node.getId(), mergeNode(old, node));
		super.visitIndex(index);
	}

	@Override
	public void visitSegment(Segment segment) {
		int id = segment.getId();

		if (id == m_minute) {
			super.visitSegment(segment);
		}
	}
}
