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

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

@Named
public class DependencyItemBuilder {

	private static final int OK = GraphConstrant.OK;

	private static final String DATABASE = GraphConstrant.DATABASE;

	private static final String PROJECT = GraphConstrant.PROJECT;

	private static final String CACHE = GraphConstrant.CACHE;

	@Inject
	private TopologyGraphConfigManager m_graphConfigManager;

	public TopologyEdge buildEdge(String domain, Dependency dependency) {
		TopologyEdge edge = new TopologyEdge();

		edge.setType(dependency.getType());
		edge.setKey(dependency.getType() + ':' + domain + ':' + dependency.getTarget());
		edge.setSelf(domain);
		edge.setTarget(dependency.getTarget());
		edge.setOpposite(false);
		edge.setWeight(1);

		Pair<Integer, String> state = m_graphConfigManager.buildEdgeState(domain, dependency);

		edge.setStatus(state.getKey());
		edge.setDes(state.getValue());
		return edge;
	}

	public TopologyNode buildNode(String domain, Index index) {
		TopologyNode node = new TopologyNode(domain);

		node.setType(PROJECT);
		node.setWeight(1);

		Pair<Integer, String> state = m_graphConfigManager.buildNodeState(domain, index);

		node.setStatus(state.getKey());
		node.setDes(state.getValue());
		return node;
	}

	public TopologyNode createCacheNode(String cache) {
		TopologyNode node = new TopologyNode(cache);

		node.setStatus(OK);
		node.setType(CACHE);
		node.setWeight(1);
		return node;
	}

	public TopologyNode createDatabaseNode(String database) {
		TopologyNode node = new TopologyNode(database);

		node.setStatus(OK);
		node.setType(DATABASE);
		node.setWeight(1);
		return node;
	}

	public TopologyNode createNode(String domain) {
		TopologyNode node = new TopologyNode(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		return node;
	}

}
