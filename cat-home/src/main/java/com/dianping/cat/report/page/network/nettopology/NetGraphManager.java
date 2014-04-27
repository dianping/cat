package com.dianping.cat.report.page.network.nettopology;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.network.nettopology.model.Connection;
import com.dianping.cat.report.page.network.nettopology.model.Interface;
import com.dianping.cat.report.page.network.nettopology.model.NetGraph;
import com.dianping.cat.report.page.network.nettopology.model.NetTopology;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class NetGraphManager implements Initializable, LogEnabled {

	@Inject
	private RemoteMetricReportService m_service;

	@Inject
	private ServerConfigManager m_manager;

	private int DATA_DELAY_TIME = 1;

	private NetGraph m_netGraph;

	protected Logger m_logger;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	@Override
	public void initialize() throws InitializationException {
		if (m_manager.isJobMachine()) {
			Threads.forGroup("Cat").start(new NetGraphBuilder());
		}
	}

	private class NetGraphBuilder implements Task {

		@Override
		public String getName() {
			return "NetGraphUpdate";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				int minute = (int) (current / 60000 % 60);
				NetGraph netGraph = new NetGraph("/config/nettopology-config.xml");
				double insum, outsum;

				for (NetTopology netTopology : netGraph.getNetTopologys()) {
					for (Connection connection : netTopology.getConnections()) {
						insum = 0;
						outsum = 0;
						for (Interface inter : connection.getFirstData()) {
							updateInterface(inter, minute);
							insum += inter.getIn();
							outsum += inter.getOut();
						}
						connection.setFirstInSum(insum);
						connection.setFirstOutSum(outsum);

						insum = 0;
						outsum = 0;
						for (Interface inter : connection.getSecondData()) {
							updateInterface(inter, minute);
							insum += inter.getIn();
							outsum += inter.getOut();
						}
						connection.setSecondInSum(insum);
						connection.setSecondOutSum(outsum);
					}
				}

				m_netGraph = netGraph;

				long duration = System.currentTimeMillis() - current;

				try {
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

		private void updateInterface(Interface inter, int minute) {
			String group = inter.getGroup();
			String domain = inter.getDomain();
			String key = inter.getKey();
			long period;

			minute -= DATA_DELAY_TIME;
			if (minute >= 0) {
				period = ModelPeriod.CURRENT.getStartTime();
			} else {
				period = ModelPeriod.LAST.getStartTime();
				minute += 60;
			}

			try {
				ModelRequest request = new ModelRequest(group, period);
				MetricReport report = m_service.invoke(request);
				MetricItem inItem = report.findOrCreateMetricItem(domain + ":Metric:" + inter.getKey() + "-in");
				MetricItem outItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-out");

				inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
				inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public NetGraph getNetGraph() {
		return m_netGraph;
	}

	public void setNetGraph(NetGraph netGraph) {
		m_netGraph = netGraph;
	}
}
