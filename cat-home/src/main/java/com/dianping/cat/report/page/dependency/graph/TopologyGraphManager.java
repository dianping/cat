package com.dianping.cat.report.page.dependency.graph;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.company.model.entity.Domain;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dal.report.TopologyGraphEntity;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.home.dependency.graph.transform.DefaultNativeParser;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class TopologyGraphManager implements Initializable, LogEnabled {

	@Inject(type = ModelService.class, value = DependencyAnalyzer.ID)
	private ModelService<DependencyReport> m_service;

	@Inject
	private DependencyItemBuilder m_itemBuilder;

	@Inject
	private ProductLineConfigManager m_productLineConfigManger;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private TopologyGraphDao m_topologyGraphDao;

	private TopologyGraphBuilder m_currentBuilder;

	private Map<Long, TopologyGraph> m_topologyGraphs = new ConcurrentHashMap<Long, TopologyGraph>();

	private Logger m_logger;

	public ProductLinesDashboard buildDependencyDashboard(long time) {
		TopologyGraph topologyGraph = queryTopologyGraph(time);
		ProductLinesDashboard dashboardGraph = new ProductLinesDashboard();
		Set<String> allDomains = new HashSet<String>();

		if (topologyGraph != null) {
			Map<String, ProductLine> groups = m_productLineConfigManger.queryApplicationProductLines();

			for (Entry<String, ProductLine> entry : groups.entrySet()) {
				String realName = entry.getValue().getTitle();

				Map<String, Domain> domains = entry.getValue().getDomains();
				for (Domain domain : domains.values()) {
					String nodeName = domain.getId();
					TopologyNode node = topologyGraph.findTopologyNode(nodeName);

					allDomains.add(nodeName);
					if (node != null) {
						dashboardGraph.addNode(realName, m_currentBuilder.cloneNode(node));
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
		TopologyGraph topylogyGraph = new TopologyGraph();

		topylogyGraph.setId(domain);
		topylogyGraph.setType(GraphConstrant.PROJECT);
		topylogyGraph.setStatus(GraphConstrant.OK);

		if (all != null && m_currentBuilder != null) {
			TopologyNode node = all.findTopologyNode(domain);

			if (node != null) {
				topylogyGraph.setDes(node.getDes());
				topylogyGraph.setStatus(node.getStatus());
				topylogyGraph.setType(node.getType());
			}
			Collection<TopologyEdge> edges = all.getEdges().values();

			for (TopologyEdge edge : edges) {
				String self = edge.getSelf();
				String target = edge.getTarget();
				TopologyEdge cloneEdge = m_currentBuilder.cloneEdge(edge);

				if (self.equals(domain)) {
					TopologyNode other = all.findTopologyNode(target);

					if (other != null) {
						topylogyGraph.addTopologyNode(m_currentBuilder.cloneNode(other));
					} else {
						topylogyGraph.addTopologyNode(m_currentBuilder.createNode(target));
					}
					edge.setOpposite(false);
					topylogyGraph.addTopologyEdge(cloneEdge);
				} else if (target.equals(domain)) {
					TopologyNode other = all.findTopologyNode(self);

					if (other != null) {
						topylogyGraph.addTopologyNode(m_currentBuilder.cloneNode(other));
					} else {
						topylogyGraph.addTopologyNode(m_currentBuilder.createNode(target));
					}
					cloneEdge.setTarget(edge.getSelf());
					cloneEdge.setSelf(edge.getTarget());
					cloneEdge.setOpposite(true);
					topylogyGraph.addTopologyEdge(cloneEdge);
				}
			}
		}
		return topylogyGraph;
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
			com.dianping.cat.home.dal.report.TopologyGraph topologyGraph = m_topologyGraphDao.findByPeriod(new Date(time),
			      TopologyGraphEntity.READSET_FULL);

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

		private Collection<String> queryAllDomains() {
			ModelRequest request = new ModelRequest(Constants.CAT, ModelPeriod.CURRENT.getStartTime());

			if (m_service.isEligable(request)) {
				ModelResponse<DependencyReport> response = m_service.invoke(request);
				DependencyReport report = response.getModel();
                if(null != report) {
                    return report.getDomainNames();
                }
			}

			return new HashSet<String>();
		}

		@Override
		public void run() {
			boolean active = TimeHelper.sleepToNextMinute();

			while (active) {
				Transaction t = Cat.newTransaction("ReloadTask", "Dependency");
				long current = System.currentTimeMillis();
				try {
					TopologyGraphBuilder builder = new TopologyGraphBuilder().setItemBuilder(m_itemBuilder);
					Collection<String> domains = queryAllDomains();

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
