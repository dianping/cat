package com.dianping.cat.report.page.dependency;

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
import com.dianping.cat.home.dependency.entity.DependencyGraph;
import com.dianping.cat.home.dependency.entity.Edge;
import com.dianping.cat.home.dependency.entity.Node;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.view.DomainNavManager;

public class GraphManager implements Initializable, LogEnabled {

	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_service;

	@Inject
	private DependencyGraphBuilder m_builder;

	private Map<Long, DependencyGraph> m_graphs = new ConcurrentHashMap<Long, DependencyGraph>(1000);

	private Logger m_logger;

	public DependencyGraph queryGraph(String domain, long time) {
		DependencyGraph graph = m_graphs.get(time);
		DependencyGraph result = new DependencyGraph();

		if (graph != null) {
			Node node = graph.findNode(domain);

			if (node != null) {
				result.setDes(node.getDes());
				result.setId(node.getId());
				result.setStatus(node.getStatus());
				result.setType(node.getType());
			}
			Collection<Edge> edges = graph.getEdges().values();

			for (Edge edge : edges) {
				String self = edge.getSelf();
				String target = edge.getTarget();

				if (self.equals(domain)) {
					Node other = graph.findNode(target);

					if (other != null) {
						result.addNode(other);
					} else {
						result.addNode(creatNode(target));
					}
					edge.setOpposite(false);
					result.addEdge(edge);
				} else if (target.equals(domain)) {
					Node other = graph.findNode(self);

					if (other != null) {
						result.addNode(other);
					} else {
						result.addNode(creatNode(target));
					}
					edge.setOpposite(true);
					result.addEdge(edge);
				}
			}
		}
		return result;
	}

	private Node creatNode(String domain) {
		Node node = new Node(domain);

		node.setStatus(DefaultDependencyGraphItemBuilder.OK);
		node.setType(DefaultDependencyGraphItemBuilder.PROJECT);
		node.setWeight(1);
		node.setDes("");
		node.setLink("");

		return node;
	}

	private class Reload implements Task {

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				Transaction t = Cat.newTransaction("Dependency", "Reload");
				try {
					long currentHour = current - current % TimeUtil.ONE_HOUR;
					long currentMinute = current - current % TimeUtil.ONE_MINUTE;
					long time = current / 1000 / 60;
					int minute = (int) (time % (60));
					String value = String.valueOf(currentHour);
					Collection<String> domains = getAllDomains();

					for (String temp : domains) {
						try {
							ModelRequest request = new ModelRequest(temp, ModelPeriod.CURRENT).setProperty("date", value);
							if (m_service.isEligable(request)) {
								ModelResponse<DependencyReport> response = m_service.invoke(request);
								DependencyReport report = response.getModel();
								DependencyGraph graph = m_graphs.get(currentMinute);

								if (graph == null) {
									graph = new DependencyGraph();
									m_graphs.put(currentMinute, graph);
								}
								m_builder.setGraph(graph).setMinute(minute);
								m_builder.visitDependencyReport(report);
							} else {
								m_logger.warn(String.format("Can't get dependency report of %s", temp));
							}
						} catch (Exception e) {
							Cat.logError(e);
							t.setStatus(e);
						}
					}
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception ex) {
					ex.printStackTrace();
					t.setStatus(ex);
				} finally {
					t.complete();
				}
				long duration = System.currentTimeMillis() - current;

				try {
					int maxDuration = 20 * 1000;
					if (duration < maxDuration) {
						Thread.sleep(maxDuration - duration);
					}
				} catch (InterruptedException e) {
					active = false;
				}
			}

		}

		private Collection<String> getAllDomains() {
			return DomainNavManager.getDomains();
		}

		@Override
		public String getName() {
			return "DependencyManagerReload";
		}

		@Override
		public void shutdown() {
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new Reload());
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
