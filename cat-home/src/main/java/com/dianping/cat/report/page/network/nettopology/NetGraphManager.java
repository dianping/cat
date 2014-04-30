package com.dianping.cat.report.page.network.nettopology;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.nettopo.entity.Connection;
import com.dianping.cat.home.nettopo.entity.Interface;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.transform.DefaultSaxParser;
import com.dianping.cat.home.nettopo.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class NetGraphManager implements Initializable, LogEnabled {

	@Inject
	private RemoteMetricReportService m_service;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	private int DATA_DELAY_TIME = 1;

	private NetGraphSet m_netGraphSet;

	protected Logger m_logger;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	public NetGraphManager() {
		m_netGraphSet = new NetGraphSet();
		InputStream is = NetGraphManager.class.getResourceAsStream("/config/default-nettopology-config.xml");
		try {
			m_netGraphSet = DefaultSaxParser.parse(is);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

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
				double insum, outsum;
				InputStream is = NetGraphManager.class.getResourceAsStream("/config/default-nettopology-config.xml");
				NetGraph netGraph;

				try {
					NetGraphSet netGraphSet = DefaultSaxParser.parse(is);
					netGraph = netGraphSet.getNetGraphs().get(null);
				} catch (Exception e) {
					netGraph = new NetGraph();
					e.printStackTrace();
				}

				for (NetTopology netTopology : netGraph.getNetTopologies()) {
					for (Connection connection : netTopology.getConnections()) {
						insum = 0;
						outsum = 0;
						for (Interface inter : connection.getInterfaces()) {
							updateInterface(inter, minute);
							insum += inter.getIn();
							outsum += inter.getOut();
						}
						connection.setInsum(insum);
						connection.setOutsum(outsum);
					}
				}

				netGraph.setMinute(minute);
				m_netGraphSet.getNetGraphs().put(minute, netGraph);

				if (minute == 59) {
					HourlyReport hourlyReport = new HourlyReport();
					hourlyReport.setType(1);
					hourlyReport.setName(Constants.REPORT_NET_TOPOLOGY);
					hourlyReport.setIp("");
					hourlyReport.setDomain("Cat");
					Date period = new Date(current / 3600000 * 3600000);
					hourlyReport.setPeriod(period);
					DefaultXmlBuilder defaultXmlBuilder = new DefaultXmlBuilder();
					String content = defaultXmlBuilder.buildXml(m_netGraphSet);
					hourlyReport.setContent(content);
					hourlyReport.setCreationDate(new Date(current));
					try {
						m_hourlyReportDao.insert(hourlyReport);
					} catch (DalException e) {
						e.printStackTrace();
					}
					
					NetGraphSet netGraphSet = new NetGraphSet();
					netGraphSet.getNetGraphs().put(null, m_netGraphSet.getNetGraphs().get(null));
					m_netGraphSet = netGraphSet;
				}

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

				if (report != null) {
					MetricItem inItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-in");
					MetricItem outItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-out");

					inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
					inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
				}
			} catch (Exception e) {
				inter.setIn(0.0);
				inter.setOut(0.0);
				Cat.logError(e);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public NetGraphSet getNetGraphSet() {
		return m_netGraphSet;
	}

	public void setNetGraphSet(NetGraphSet netGraphSet) {
		m_netGraphSet = netGraphSet;
	}

	public ArrayList<Pair<String, String>> getNetGraphData(int minute) {
		JsonBuilder jb = new JsonBuilder();
		ArrayList<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		NetGraph netGraph;

		if (m_netGraphSet.getNetGraphs().size() == 1) {
			netGraph = m_netGraphSet.getNetGraphs().get(null);
		} else {
			netGraph = m_netGraphSet.getNetGraphs().get(minute);
		}

		if (netGraph != null) {
			for (NetTopology netTopology : netGraph.getNetTopologies()) {
				String topoName = netTopology.getName();
				String data = jb.toJson(netTopology);
				netGraphData.add(new Pair<String, String>(topoName, data));
			}
		}

		return netGraphData;
	}
}
