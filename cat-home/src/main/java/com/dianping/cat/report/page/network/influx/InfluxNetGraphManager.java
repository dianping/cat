package com.dianping.cat.report.page.network.influx;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.network.entity.NetGraph;
import com.dianping.cat.home.network.entity.NetTopology;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.server.MetricService;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

public class InfluxNetGraphManager implements Initializable, LogEnabled {

	@Inject
	private MetricService m_metricService;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private InfluxNetGraphBuilder m_netGraphBuilder;

	@Inject
	private AlertManager m_alertManager;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	protected Logger m_logger;

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private Map<Long, NetGraph> m_netGraphs = new LinkedHashMap<Long, NetGraph>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<Long, NetGraph> eldest) {
			return size() > 60;
		}

	};

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Pair<String, String>> getNetGraphData(Date start, int minute) {
		JsonBuilder jb = new JsonBuilder();
		List<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		long startTime = start.getTime();
		long current = startTime + TimeHelper.ONE_MINUTE * minute;
		NetGraph graph = m_netGraphs.get(current);

		if (graph == null) {
			graph = buildGraph(new Date(current));
		}

		for (NetTopology netTopology : graph.getNetTopologies()) {
			String topoName = netTopology.getName();
			String data = jb.toJson(netTopology);

			netGraphData.add(new Pair<String, String>(topoName, data));
		}
		return netGraphData;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isJobMachine()) {
			Threads.forGroup("cat").start(new NetGraphReloader());
		}
	}

	private NetGraph buildGraph(Date minute) {
		NetGraph netGraphTemplate = m_netGraphConfigManager.getConfig().getNetGraphs().get(0);
		List<AlertEntity> alertKeys = m_alertManager.queryLastestAlarmKey(5);
		NetGraph graph = m_netGraphBuilder.buildGraphSet(netGraphTemplate, minute, alertKeys);

		return graph;
	}

	private class NetGraphReloader implements Task {

		@Override
		public String getName() {
			return "NetGraphUpdate";
		}

		@Override
		public void run() {
			boolean active = TimeHelper.sleepToNextMinute(TimeHelper.ONE_SECOND * 10);

			while (active) {
				long start = System.currentTimeMillis();

				try {
					Transaction t = Cat.newTransaction("ReloadTask", "networkGraph");

					try {
						Date minute = TimeHelper.getCurrentMinute(-1);
						long time = minute.getTime();
						NetGraph netGraph = m_netGraphs.get(time);

						if (netGraph == null) {
							NetGraph graph = buildGraph(minute);

							m_netGraphs.put(time, graph);
						}
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
					} finally {
						t.complete();
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					long duration = System.currentTimeMillis() - start;

					if (duration < DURATION) {
						Thread.sleep(DURATION - duration);
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
