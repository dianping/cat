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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dal.report.TopologyGraphEntity;
import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.home.dependency.graph.transform.DefaultNativeParser;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

@Named
public class TopologyGraphManager implements Initializable, LogEnabled {

	@Inject(type = ModelService.class, value = DependencyAnalyzer.ID)
	private ModelService<DependencyReport> m_service;

	@Inject
	private DependencyItemBuilder m_itemBuilder;

	@Inject
	private TopoGraphFormatConfigManager m_configManager;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private TopologyGraphDao m_topologyGraphDao;

	private TopologyGraphBuilder m_currentBuilder;

	private Map<Long, TopologyGraph> m_topologyGraphs = new ConcurrentHashMap<Long, TopologyGraph>();

	private Logger m_logger;

	public Set<TopologyEdge> buildEdges(Set<String> domains, Date start, Date end) {
		Set<TopologyEdge> result = new HashSet<TopologyEdge>();

		for (long current = start.getTime(); current <= end.getTime(); current = current + TimeHelper.ONE_MINUTE) {
			result.addAll(buildEdges(domains, current));
		}
		return result;
	}

	public Set<TopologyEdge> buildEdges(Set<String> domains, long time) {
		TopologyGraph topologyGraph = queryTopologyGraph(time);
		Set<TopologyEdge> result = new HashSet<TopologyEdge>();

		if (topologyGraph != null) {
			Map<String, TopologyEdge> edges = topologyGraph.getEdges();

			for (TopologyEdge edge : edges.values()) {
				String self = edge.getSelf();
				String to = edge.getTarget();

				if (domains.contains(self) && domains.contains(to)) {
					result.add(m_currentBuilder.cloneEdge(edge));
				}
			}
		}
		return result;
	}

	public ProductLinesDashboard buildDependencyDashboard(long time) {
		TopologyGraph topologyGraph = queryTopologyGraph(time);
		ProductLinesDashboard dashboardGraph = new ProductLinesDashboard();
		Set<String> allDomains = new HashSet<String>();

		if (topologyGraph != null) {
			List<ProductLine> productLines = m_configManager.queryProduct();

			for (ProductLine entry : productLines) {
				String productId = entry.getId();
				List<Domain> domains = entry.getDomains();

				for (Domain domain : domains) {
					String nodeName = domain.getId();
					TopologyNode node = topologyGraph.findTopologyNode(nodeName);

					allDomains.add(nodeName);
					if (node != null) {
						dashboardGraph.addNode(productId, m_currentBuilder.cloneNode(node));
					}
				}
			}
			Map<String, TopologyEdge> edges = topologyGraph.getEdges();

			for (TopologyEdge edge : edges.values()) {
				String self = edge.getSelf();
				String to = edge.getTarget();

				if (allDomains.contains(self) && allDomains.contains(to)) {
					dashboardGraph.addEdge(m_currentBuilder.cloneEdge(edge));
				}
			}
		}
		return dashboardGraph.sortByNodeNumber();
	}

	public TopologyGraph buildTopologyGraph(String domain, long time) {
		TopologyGraph all = queryTopologyGraph(time);
		TopologyGraph topologyGraph = new TopologyGraph();

		topologyGraph.setId(domain);
		topologyGraph.setType(GraphConstrant.PROJECT);
		topologyGraph.setStatus(GraphConstrant.OK);

		if (all != null && m_currentBuilder != null) {
			TopologyNode node = all.findTopologyNode(domain);

			if (node != null) {
				topologyGraph.setDes(node.getDes());
				topologyGraph.setStatus(node.getStatus());
				topologyGraph.setType(node.getType());
			}
			Collection<TopologyEdge> edges = all.getEdges().values();

			for (TopologyEdge edge : edges) {
				String self = edge.getSelf();
				String target = edge.getTarget();
				TopologyEdge cloneEdge = m_currentBuilder.cloneEdge(edge);

				if (self.equals(domain)) {
					TopologyNode other = all.findTopologyNode(target);

					if (other != null) {
						topologyGraph.addTopologyNode(m_currentBuilder.cloneNode(other));
					} else {
						topologyGraph.addTopologyNode(m_currentBuilder.createNode(target));
					}
					edge.setOpposite(false);
					topologyGraph.addTopologyEdge(cloneEdge);
				} else if (target.equals(domain)) {
					TopologyNode other = all.findTopologyNode(self);

					if (other != null) {
						topologyGraph.addTopologyNode(m_currentBuilder.cloneNode(other));
					} else {
						topologyGraph.addTopologyNode(m_currentBuilder.createNode(target));
					}
					cloneEdge.setTarget(edge.getSelf());
					cloneEdge.setSelf(edge.getTarget());
					cloneEdge.setOpposite(true);
					topologyGraph.addTopologyEdge(cloneEdge);
				}
			}
		}
		return topologyGraph;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_manager.isJobMachine()) {
			Threads.forGroup("cat").start(new DependencyReloadTask());
		}
	}

	public TopologyGraph queryGraphFromDB(long time) {
		try {
			com.dianping.cat.home.dal.report.TopologyGraph topologyGraph = m_topologyGraphDao
									.findByPeriod(new Date(time),	TopologyGraphEntity.READSET_FULL);

			if (topologyGraph != null) {
				byte[] content = topologyGraph.getContent();

				return DefaultNativeParser.parse(content);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	private TopologyGraph queryGraphFromMemory(long time) {
		TopologyGraph graph = m_topologyGraphs.get(time);
		long current = System.currentTimeMillis();
		long minute = current - current % TimeHelper.ONE_MINUTE;

		if ((minute - time) <= 3 * TimeHelper.ONE_MINUTE && graph == null) {
			graph = m_topologyGraphs.get(time - TimeHelper.ONE_MINUTE);

			if (graph == null) {
				graph = m_topologyGraphs.get(time - TimeHelper.ONE_MINUTE * 2);
			}
		}
		return graph;
	}

	private TopologyGraph queryTopologyGraph(long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period.isHistorical()) {
			return queryGraphFromDB(time);
		} else {
			return queryGraphFromMemory(time);
		}
	}

	private class DependencyReloadTask implements Task {

		private void buildDependencyInfo(TopologyGraphBuilder builder, String domain) {
			if (m_serverFilterConfigManager.validateDomain(domain)) {
				ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT.getStartTime());

				if (m_service.isEligable(request)) {
					ModelResponse<DependencyReport> response = m_service.invoke(request);
					DependencyReport report = response.getModel();

					if (report != null) {
						builder.visitDependencyReport(report);
					}
				} else {
					m_logger.warn(String.format("Can't get dependency report of %s", domain));
				}
			}
		}

		@Override
		public String getName() {
			return "TopologyGraphReload";
		}

		@Override
		public void run() {
			boolean active = TimeHelper.sleepToNextMinute();

			while (active) {
				Transaction t = Cat.newTransaction("ReloadTask", "Dependency");
				long current = System.currentTimeMillis();
				try {
					TopologyGraphBuilder builder = new TopologyGraphBuilder().setItemBuilder(m_itemBuilder);
					Collection<String> domains = m_projectService.findAllDomains();

					for (String domain : domains) {
						try {
							buildDependencyInfo(builder, domain);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
					Map<Long, TopologyGraph> graphs = builder.getGraphs();

					for (Entry<Long, TopologyGraph> entry : graphs.entrySet()) {
						m_topologyGraphs.put(entry.getKey(), entry.getValue());

						m_topologyGraphs.remove(entry.getKey() - TimeHelper.ONE_HOUR * 2);
					}
					m_currentBuilder = builder;
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					m_logger.error(e.getMessage(), e);
					t.setStatus(e);
				} finally {
					t.complete();
				}
				long duration = System.currentTimeMillis() - current;

				try {
					int maxDuration = 60 * 1000;
					if (duration < maxDuration) {
						Thread.sleep(maxDuration - duration);
					}
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
