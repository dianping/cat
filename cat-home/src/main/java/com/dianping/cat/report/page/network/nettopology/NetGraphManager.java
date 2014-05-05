package com.dianping.cat.report.page.network.nettopology;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.nettopo.entity.Anchor;
import com.dianping.cat.home.nettopo.entity.Connection;
import com.dianping.cat.home.nettopo.entity.Interface;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.entity.Switch;
import com.dianping.cat.home.nettopo.transform.DefaultSaxParser;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.service.ModelRequest;

public class NetGraphManager implements Initializable, LogEnabled {

	@Inject
	private RemoteMetricReportService m_service;

	@Inject
	private ServerConfigManager m_manager;

	private NetGraph m_netGraphTemplate;

	private NetGraphSet m_currentNetGraphSet;

	private NetGraphSet m_lastNetGraphSet;

	@Inject
	private ReportService m_reportService;

	protected Logger m_logger;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	public NetGraphManager() {
		InputStream is = NetGraphManager.class.getResourceAsStream("/config/default-nettopology-config.xml");
		try {
			NetGraphSet netGraphSet = DefaultSaxParser.parse(is);
			m_netGraphTemplate = netGraphSet.getNetGraphs().get(null);
		} catch (Exception e) {
			m_netGraphTemplate = new NetGraph();
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
				InputStream is = NetGraphManager.class.getResourceAsStream("/config/default-nettopology-config.xml");

				try {
					NetGraphSet netGraphSet = DefaultSaxParser.parse(is);
					m_netGraphTemplate = netGraphSet.getNetGraphs().get(null);
				} catch (Exception e) {
					m_netGraphTemplate = new NetGraph();
					Cat.logError(e);
				}

				m_lastNetGraphSet = buildSet(current - TimeUtil.ONE_HOUR);
				m_currentNetGraphSet = buildSet(current);

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

	}

	public NetGraphSet buildSet(long time) {
		Set<String> groupSet = new HashSet<String>();
		HashMap<String, MetricReport> reportSet = new LinkedHashMap<String, MetricReport>();
		long period = time - time % TimeUtil.ONE_HOUR;
		NetGraphSet netGraphSet = new NetGraphSet();

		for (NetTopology netTopology : m_netGraphTemplate.getNetTopologies()) {
			for (Connection connection : netTopology.getConnections()) {
				for (Interface inter : connection.getInterfaces()) {
					groupSet.add(inter.getGroup());
				}
			}
		}

		for (String group : groupSet) {
			ModelRequest request = new ModelRequest(group, period);
			MetricReport report = m_service.invoke(request);
			reportSet.put(group, report);
		}

		for (int i = 0; i <= 59; i++) {
			NetGraph netGraph = CopyNetGraph(m_netGraphTemplate);
			for (NetTopology netTopology : netGraph.getNetTopologies()) {
				for (Connection connection : netTopology.getConnections()) {
					double insum = 0;
					double outsum = 0;
					for (Interface inter : connection.getInterfaces()) {
						String group = inter.getGroup();
						updateInterface(inter, reportSet.get(group), i);
						insum += inter.getIn();
						outsum += inter.getOut();
					}
					connection.setInsum(insum);
					connection.setOutsum(outsum);
				}
			}
			netGraph.setMinute(i);
			netGraphSet.addNetGraph(netGraph);
		}

		return netGraphSet;
	}

	private NetGraph CopyNetGraph(NetGraph netGraphA) {
		NetGraph netGraphB = new NetGraph();
		for (NetTopology netTopologyA : netGraphA.getNetTopologies()) {
			NetTopology netTopologyB = new NetTopology();

			for (Anchor anchorA : netTopologyA.getAnchors()) {
				Anchor anchorB = new Anchor();
				anchorB.setName(anchorA.getName());
				anchorB.setX(anchorA.getX());
				anchorB.setY(anchorA.getY());
				netTopologyB.addAnchor(anchorB);
			}

			for (Switch switchA : netTopologyA.getSwitchs()) {
				Switch switchB = new Switch();
				switchB.setName(switchA.getName());
				switchB.setX(switchA.getX());
				switchB.setY(switchA.getY());
				netTopologyB.addSwitch(switchB);
			}

			for (Connection connectionA : netTopologyA.getConnections()) {
				Connection connectionB = new Connection();
				for (Interface interA : connectionA.getInterfaces()) {
					Interface interB = new Interface();
					interB.setDomain(interA.getDomain());
					interB.setGroup(interA.getGroup());
					interB.setKey(interA.getKey());
					interB.setIn(interA.getIn());
					interB.setOut(interA.getOut());
					interB.setState(interA.getState());
					connectionB.addInterface(interB);
				}
				connectionB.setInsum(connectionA.getInsum());
				connectionB.setOutsum(connectionA.getOutsum());
				connectionB.setFrom(connectionA.getFrom());
				connectionB.setTo(connectionA.getTo());
				connectionB.setState(connectionA.getState());
				netTopologyB.addConnection(connectionB);
			}

			netGraphB.addNetTopology(netTopologyB);
		}

		return netGraphB;
	}

	private void updateInterface(Interface inter, MetricReport report, int minute) {
		String domain = inter.getDomain();
		String key = inter.getKey();

		try {
			MetricItem inItem = report.findOrCreateMetricItem(domain + ":Metric:" + inter.getKey() + "-in");
			MetricItem outItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-out");

			inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
		} catch (Exception e) {
			inter.setIn(0.0);
			inter.setOut(0.0);
			Cat.logError(e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public NetGraphSet getNetGraphSet() {
		return m_currentNetGraphSet;
	}

	public void setNetGraphSet(NetGraphSet netGraphSet) {
		m_currentNetGraphSet = netGraphSet;
	}

	public ArrayList<Pair<String, String>> getNetGraphData(Date start, int minute) {
		JsonBuilder jb = new JsonBuilder();
		ArrayList<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		long current = System.currentTimeMillis();
		long currentHours = current - current % TimeUtil.ONE_HOUR;
		long startTime = start.getTime();

		NetGraphSet netGraphSet;
		if (startTime == currentHours) {
			netGraphSet = m_currentNetGraphSet;
		} else if (startTime == currentHours - TimeUtil.ONE_HOUR) {
			netGraphSet = m_lastNetGraphSet;
		} else if (startTime < currentHours - TimeUtil.ONE_HOUR) {
			netGraphSet = m_reportService.queryNetTopologyReport("Cat", start, null);
		} else {
			netGraphSet = null;
		}

		if (netGraphSet != null) {
			NetGraph netGraph = netGraphSet.getNetGraphs().get(minute);

			if (netGraph != null) {
				for (NetTopology netTopology : netGraph.getNetTopologies()) {
					String topoName = netTopology.getName();
					String data = jb.toJson(netTopology);
					netGraphData.add(new Pair<String, String>(topoName, data));
				}
			}
		}

		return netGraphData;
	}
}
