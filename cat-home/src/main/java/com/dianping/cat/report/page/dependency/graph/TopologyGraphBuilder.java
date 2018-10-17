/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.dependency.graph;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

@Named
public class TopologyGraphBuilder extends BaseVisitor {

	@Inject
	private DependencyItemBuilder m_itemBuilder;

	private String m_domain;

	private Map<Long, TopologyGraph> m_graphs = new HashMap<Long, TopologyGraph>();

	private int m_minute;

	private Date m_date;

	private Set<String> m_pigeonServices = new HashSet<String>(Arrays.asList("Service", "PigeonService", "PigeonServer"));

	public TopologyEdge cloneEdge(TopologyEdge edge) {
		TopologyEdge result = new TopologyEdge();

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

	public TopologyNode cloneNode(TopologyNode node) {
		TopologyNode result = new TopologyNode();

		result.setDes(node.getDes());
		result.setId(node.getId());
		result.setLink(node.getLink());
		result.setStatus(node.getStatus());
		result.setType(node.getType());
		result.setWeight(node.getWeight());
		return result;
	}

	public TopologyNode createNode(String domain) {
		return m_itemBuilder.createNode(domain);
	}

	private TopologyGraph findOrCreateGraph() {
		long time = m_date.getTime() + m_minute * TimeHelper.ONE_MINUTE;
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
		if (StringUtils.isEmpty(old)) {
			return des;
		} else if (StringUtils.isEmpty(des)) {
			return old;
		} else {
			return old + des;
		}
	}

	private TopologyEdge mergeEdge(TopologyEdge old, TopologyEdge edge) {
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

	private TopologyNode mergeNode(TopologyNode old, TopologyNode node) {
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

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();

		if (!m_pigeonServices.contains(type)) {
			TopologyEdge edge = m_itemBuilder.buildEdge(m_domain, dependency);
			TopologyGraph graph = findOrCreateGraph();
			TopologyEdge old = graph.findTopologyEdge(edge.getKey());

			graph.getEdges().put(edge.getKey(), mergeEdge(old, edge));
			if ("Database".equals(type)) {
				String target = dependency.getTarget();
				TopologyNode nodeOld = graph.findTopologyNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, m_itemBuilder.createDatabaseNode(target)));
			} else if ("Cache".equals(type)) {
				String target = dependency.getTarget();
				TopologyNode nodeOld = graph.findTopologyNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, m_itemBuilder.createCacheNode(target)));
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
		TopologyNode node = m_itemBuilder.buildNode(m_domain, index);
		TopologyNode old = graph.findTopologyNode(node.getId());

		graph.getNodes().put(node.getId(), mergeNode(old, node));
	}

	@Override
	public void visitSegment(Segment segment) {
		m_minute = segment.getId();
		super.visitSegment(segment);
	}

	public TopologyGraphBuilder setItemBuilder(DependencyItemBuilder itemBuilder) {
		m_itemBuilder = itemBuilder;
		return this;
	}

}
