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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public class ProductLinesDashboard {

	private Map<String, List<TopologyNode>> m_productLines = new LinkedHashMap<String, List<TopologyNode>>();

	private List<TopologyEdge> m_edges = new ArrayList<TopologyEdge>();

	private transient Map<String, TopologyNode> m_nodes = new LinkedHashMap<String, TopologyNode>();

	public ProductLinesDashboard addEdge(TopologyEdge edge) {
		m_edges.add(edge);
		return this;
	}

	public ProductLinesDashboard addNode(String productLine, TopologyNode node) {
		List<TopologyNode> nodeList = m_productLines.get(productLine);

		if (nodeList == null) {
			nodeList = new ArrayList<TopologyNode>();
			m_productLines.put(productLine, nodeList);
		}

		nodeList.add(node);
		return this;
	}

	public boolean exsit(TopologyNode node) {
		return m_nodes.containsKey(node.getId());
	}

	public List<TopologyEdge> getEdges() {
		return m_edges;
	}

	public Map<String, List<TopologyNode>> getNodes() {
		return m_productLines;
	}

	public String toJson() {
		String str = new JsonBuilder().toJson(this);

		return str;
	}

	public ProductLinesDashboard sortByNodeNumber() {
		List<Entry<String, List<TopologyNode>>> list = new ArrayList<Entry<String, List<TopologyNode>>>(
								m_productLines.entrySet());
		Map<String, List<TopologyNode>> nodes = new LinkedHashMap<String, List<TopologyNode>>();

		Collections.sort(list, new Comparator<Entry<String, List<TopologyNode>>>() {

			@Override
			public int compare(Entry<String, List<TopologyNode>> o1, Entry<String, List<TopologyNode>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
		});

		for (Entry<String, List<TopologyNode>> l : list) {
			nodes.put(l.getKey(), l.getValue());
		}
		m_productLines = nodes;
		return this;
	}
}
