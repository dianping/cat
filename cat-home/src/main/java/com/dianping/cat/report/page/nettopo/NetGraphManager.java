package com.dianping.cat.report.page.nettopo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.nettopo.model.Connection;
import com.dianping.cat.report.page.nettopo.model.Interface;
import com.dianping.cat.report.page.nettopo.model.NetGraph;
import com.dianping.cat.report.page.nettopo.model.NetTopology;
import com.dianping.cat.report.page.nettopo.model.Switch;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class NetGraphManager implements Initializable {

	@Inject
	private RemoteMetricReportService m_service;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private int DATA_DELAY_TIME = 1;

	@Inject
	private Map<Long, NetGraph> netGraphs = new LinkedHashMap<Long, NetGraph>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<Long, NetGraph> eldest) {
			return size() > 5;
		}
	};

	@Inject
	private Long currentKey = new Long(0);

	public Map<Long, NetGraph> getNetGraphs() {
		return netGraphs;
	}

	public void setNetGraphs(Map<Long, NetGraph> netGraphs) {
		this.netGraphs = netGraphs;
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new NetGraphUpdate());
	}

	private class NetGraphUpdate implements Task {

		@Override
		public String getName() {
			return "NetGraphUpdate";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				long key = current;
				int minute = (int) (current / 60000 % 60);

				NetGraph netGraph = new NetGraph("/config/netconfig.xml");

				Pair<String, Integer> state;
				Switch sw;
				for (NetTopology netTopology : netGraph.getNetTopologys()) {
					for (Connection connection : netTopology.getConnections()) {
						for (Interface in : connection.getFirstData()) {
							updateInterface(in, minute);
						}
						state = connection.setFirstState();
						sw = netTopology.getSwitchs().get(state.getKey());
						if (sw != null)
							sw.setState(state.getValue());

						for (Interface in : connection.getSecondData()) {
							updateInterface(in, minute);
						}
						state = connection.setSecondState();
						sw = netTopology.getSwitchs().get(state.getKey());
						if (sw != null)
							sw.setState(state.getValue());
					}
				}

				netGraphs.put(key, netGraph);
				synchronized (currentKey) {
					currentKey = key;
				}

				long duration = System.currentTimeMillis() - current;

				try {
					if (duration < DURATION) {
						Thread.sleep(DURATION - duration);
					}
				} catch (InterruptedException e) {
					Cat.logError(e);
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}

		private void updateInterface(Interface in, int minute) {
			String group = in.getGroup();
			String domain = in.getDomain();
			String key = in.getKey();
			Pair<String, Integer> server = new Pair<String, Integer>("10.1.1.167", 80);

			long period;
			minute -= DATA_DELAY_TIME;
			if (minute >= 0) {
				period = ModelPeriod.CURRENT.getStartTime();
			} else {
				period = ModelPeriod.LAST.getStartTime();
				minute += 60;
			}

			ModelRequest request = new ModelRequest(group, period);
			MetricItem metricItem;
			try {
				MetricReport report = m_service.invoke(request, server);
				metricItem = report.findMetricItem(domain + ":Metric:" + in.getKey() + "-in");
				in.setIn(metricItem.getSegments().get(minute).getSum() / 60 * 8);
				metricItem = report.findMetricItem(domain + ":Metric:" + key + "-out");
				in.setOut(metricItem.getSegments().get(minute).getSum() / 60 * 8);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public NetGraph getLastGraph() {
		long key;
		synchronized (currentKey) {
			key = currentKey.longValue();
		}

		return netGraphs.get(key);
	}
}