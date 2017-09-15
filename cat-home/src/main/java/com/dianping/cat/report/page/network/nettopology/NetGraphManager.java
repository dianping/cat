package com.dianping.cat.report.page.network.nettopology;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.network.entity.Connection;
import com.dianping.cat.home.network.entity.Interface;
import com.dianping.cat.home.network.entity.NetGraph;
import com.dianping.cat.home.network.entity.NetGraphSet;
import com.dianping.cat.home.network.entity.NetTopology;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.AlertInfo;
import com.dianping.cat.report.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.network.service.NetTopologyReportService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class NetGraphManager implements Initializable, LogEnabled {

	@Inject(type = ModelService.class, value = MetricAnalyzer.ID)
	private ModelService<MetricReport> m_service;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private NetTopologyReportService m_reportService;

	@Inject
	private NetGraphBuilder m_netGraphBuilder;

	@Inject
	private AlertInfo m_alertInfo;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	private NetGraphSet m_currentNetGraphSet;

	private NetGraphSet m_lastNetGraphSet;

	protected Logger m_logger;

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Pair<String, String>> getNetGraphData(Date start, int minute) {
		JsonBuilder jb = new JsonBuilder();
		List<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		long current = System.currentTimeMillis();
		long currentHours = current - current % TimeHelper.ONE_HOUR;
		long startTime = start.getTime();
		NetGraphSet netGraphSet = null;

		if (startTime >= currentHours) {
			netGraphSet = m_currentNetGraphSet;
		} else if (startTime == currentHours - TimeHelper.ONE_HOUR) {
			netGraphSet = m_lastNetGraphSet;
		} else {
			netGraphSet = m_reportService.queryReport(Constants.CAT, start,
			      new Date(start.getTime() + TimeHelper.ONE_HOUR));
		}

		if (netGraphSet != null) {
			NetGraph netGraph = netGraphSet.findNetGraph(minute);

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

	@Override
	public void initialize() throws InitializationException {
		//if (m_serverConfigManager.isJobMachine()) {
		//	Threads.forGroup("cat").start(new NetGraphReloader());
		//}
	}

	private Set<String> queryAllGroups(NetGraph netGraphTemplate) {
		Set<String> groups = new HashSet<String>();

		for (NetTopology netTopology : netGraphTemplate.getNetTopologies()) {
			for (Connection connection : netTopology.getConnections()) {
				for (Interface inter : connection.getInterfaces()) {
					groups.add(inter.getGroup());
				}
			}
		}
		return groups;
	}

	private Map<String, MetricReport> queryMetricReports(Set<String> groups, ModelPeriod period) {
		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();

		for (String group : groups) {
			ModelRequest request = new ModelRequest(group, period);
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();

			reports.put(group, report);
		}
		return reports;
	}

	private class NetGraphReloader implements Task {

		@Override
		public String getName() {
			return "NetGraphUpdate";
		}

		@Override
		public void run() {
			boolean active = TimeHelper.sleepToNextMinute();

			while (active) {
				long start = System.currentTimeMillis();
				try {
					Transaction t = Cat.newTransaction("ReloadTask", "NetGraph");

					try {
						NetGraph netGraphTemplate = m_netGraphConfigManager.getConfig().getNetGraphs().get(0);
						Set<String> groups = queryAllGroups(netGraphTemplate);
						Map<String, MetricReport> currentMetricReports = queryMetricReports(groups, ModelPeriod.CURRENT);
						List<AlertMetric> alertKeys = m_alertInfo.queryLastestAlarmKey(5);

						m_currentNetGraphSet = m_netGraphBuilder.buildGraphSet(netGraphTemplate, currentMetricReports,
						      alertKeys);

						Map<String, MetricReport> lastHourReports = queryMetricReports(groups, ModelPeriod.LAST);

						m_lastNetGraphSet = m_netGraphBuilder.buildGraphSet(netGraphTemplate, lastHourReports,
						      new ArrayList<AlertMetric>());
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
