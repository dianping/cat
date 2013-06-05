package com.dianping.cat.report.page.dependency.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.view.DomainNavManager;

public class TopologyGraphManager implements Initializable, LogEnabled {

	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_service;

	@Inject
	private TopologyGraphBuilder m_graphBuilder;

	private Map<Long, TopologyGraph> m_topologyGraphs = new ConcurrentHashMap<Long, TopologyGraph>(360);

	private Logger m_logger;

	private static final String DEPENDENCY = "Dependency";

	public TopologyGraph buildGraphByDomainTime(String domain, long time) {
		TopologyGraph graph = m_topologyGraphs.get(time);
		TopologyGraph result = new TopologyGraph();
		long current = System.currentTimeMillis();
		long minute = current - current % TimeUtil.ONE_MINUTE;

		if (minute == time && graph == null) {
			graph = m_topologyGraphs.get(time - TimeUtil.ONE_MINUTE);
		}
		result.setId(domain);
		result.setType(GraphConstrant.PROJECT);
		result.setStatus(GraphConstrant.OK);

		if (graph != null) {
			Node node = graph.findNode(domain);

			if (node != null) {
				result.setDes(node.getDes());
				result.setStatus(node.getStatus());
				result.setType(node.getType());
			}
			Collection<Edge> edges = graph.getEdges().values();

			for (Edge edge : edges) {
				String self = edge.getSelf();
				String target = edge.getTarget();
				Edge cloneEdge = m_graphBuilder.cloneEdge(edge);

				if (self.equals(domain)) {
					Node other = graph.findNode(target);

					if (other != null) {
						result.addNode(m_graphBuilder.cloneNode(other));
					} else {
						result.addNode(m_graphBuilder.createNode(target));
					}
					edge.setOpposite(false);
					result.addEdge(cloneEdge);
				} else if (target.equals(domain)) {
					Node other = graph.findNode(self);

					if (other != null) {
						result.addNode(m_graphBuilder.cloneNode(other));
					} else {
						result.addNode(m_graphBuilder.createNode(target));
					}
					cloneEdge.setTarget(edge.getSelf());
					cloneEdge.setSelf(edge.getTarget());
					cloneEdge.setOpposite(true);
					result.addEdge(cloneEdge);
				}
			}
		}
		return result;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new Reload());
	}

	private class Reload implements Task {

		private void buildGraph(List<DependencyReport> reports) {
			Transaction t = Cat.newTransaction(DEPENDENCY, "BuildGraph");
			try {
				m_graphBuilder.getGraphs().clear();
				for (DependencyReport report : reports) {
					m_graphBuilder.visitDependencyReport(report);
				}
				Map<Long, TopologyGraph> graphs = m_graphBuilder.getGraphs();

				for (Entry<Long, TopologyGraph> entry : graphs.entrySet()) {
					m_topologyGraphs.put(entry.getKey(), entry.getValue());
				}
				t.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
			}
		}

		private List<DependencyReport> fetchReport(Collection<String> domains) {
			List<DependencyReport> reports = new ArrayList<DependencyReport>();
			long current = System.currentTimeMillis();
			long currentHour = current - current % TimeUtil.ONE_HOUR;
			Transaction t = Cat.newTransaction(DEPENDENCY, "FetchReport");

			try {
				for (String temp : domains) {
					try {
						ModelRequest request = new ModelRequest(temp, ModelPeriod.CURRENT).setProperty("date",
						      String.valueOf(currentHour));
						if (m_service.isEligable(request)) {
							ModelResponse<DependencyReport> response = m_service.invoke(request);
							DependencyReport report = response.getModel();

							if (report != null) {
								reports.add(report);
							}
						} else {
							m_logger.warn(String.format("Can't get dependency report of %s", temp));
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				t.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
			}
			return reports;
		}

		@Override
		public String getName() {
			return "TopologyGraphReload";
		}

		private Collection<String> queryAllDomains() {
			return DomainNavManager.getDomains();
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				Transaction t = Cat.newTransaction(DEPENDENCY, "Reload");
				long current = System.currentTimeMillis();
				try {
					Collection<String> domains = queryAllDomains();
					buildGraph(fetchReport(domains));
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					m_logger.error(e.getMessage(), e);
					t.setStatus(e);
				} finally {
					t.complete();
				}
				long duration = System.currentTimeMillis() - current;

				try {
					int maxDuration = 15 * 1000;
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
