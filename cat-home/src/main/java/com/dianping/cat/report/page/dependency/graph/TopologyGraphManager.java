package com.dianping.cat.report.page.dependency.graph;

import java.util.Collection;
import java.util.Map;
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
				Edge cloneEdge = cloneEdge(edge);

				if (self.equals(domain)) {
					Node other = graph.findNode(target);

					if (other != null) {
						result.addNode(cloneNode(other));
					} else {
						result.addNode(m_graphBuilder.createNode(target));
					}
					edge.setOpposite(false);
					result.addEdge(cloneEdge);
				} else if (target.equals(domain)) {
					Node other = graph.findNode(self);

					if (other != null) {
						result.addNode(cloneNode(other));
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new Reload());
	}

	private class Reload implements Task {

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
				long current = System.currentTimeMillis();
				Transaction t = Cat.newTransaction("Dependency", "Reload");
				try {
					long currentHour = current - current % TimeUtil.ONE_HOUR;
					long currentMinute = current - current % TimeUtil.ONE_MINUTE;
					long lastMinute = currentMinute - TimeUtil.ONE_MINUTE;
					long time = current / 1000 / 60;
					int minute = (int) (time % (60));
					String value = String.valueOf(currentHour);
					Collection<String> domains = queryAllDomains();
					TopologyGraph currentGraph = new TopologyGraph();
					TopologyGraph lastGraph = new TopologyGraph();

					for (String temp : domains) {
						try {
							ModelRequest request = new ModelRequest(temp, ModelPeriod.CURRENT).setProperty("date", value);
							if (m_service.isEligable(request)) {
								ModelResponse<DependencyReport> response = m_service.invoke(request);
								DependencyReport report = response.getModel();

								m_graphBuilder.setCurrentGraph(currentGraph).setLastGraph(lastGraph).setMinute(minute);
								m_graphBuilder.visitDependencyReport(report);
							} else {
								m_logger.warn(String.format("Can't get dependency report of %s", temp));
							}
						} catch (Exception e) {
							Cat.logError(e);
							t.setStatus(e);
						}
					}
					m_topologyGraphs.put(currentMinute, currentGraph);
					m_topologyGraphs.put(lastMinute, lastGraph);
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception ex) {
					m_logger.error(ex.getMessage(), ex);
					t.setStatus(ex);
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
