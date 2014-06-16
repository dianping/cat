package com.dianping.cat.report.page.network.nettopology;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.nettopo.entity.Connection;
import com.dianping.cat.home.nettopo.entity.Interface;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.alert.AlertInfo;
import com.dianping.cat.report.task.alert.RemoteMetricReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.NetGraphConfigManager;

public class NetGraphManager implements Initializable, LogEnabled {

	@Inject
	private RemoteMetricReportService m_service;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private ReportService m_reportService;

	@Inject
	private NetGraphBuilder m_netGraphBuilder;
	
	@Inject
	private AlertInfo m_alertInfo;
	
	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;
	
	private NetGraphSet m_currentNetGraphSet;

	private NetGraphSet m_lastNetGraphSet;

	protected Logger m_logger;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Pair<String, String>> getNetGraphData(Date start, int minute) {
		JsonBuilder jb = new JsonBuilder();
		List<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		long current = System.currentTimeMillis();
		long currentHours = current - current % TimeUtil.ONE_HOUR;
		long startTime = start.getTime();
		NetGraphSet netGraphSet = null;

		if (startTime >= currentHours) {
			netGraphSet = m_currentNetGraphSet;
		} else if (startTime == currentHours - TimeUtil.ONE_HOUR) {
			netGraphSet = m_lastNetGraphSet;
		} else {
			netGraphSet = m_reportService.queryNetTopologyReport(Constants.CAT, start, null);
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
		if (m_serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat").start(new NetGraphReloader());
		}
	}

	private Map<String, MetricReport> queryMetricReports(Set<String> groups, Date date) {
		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();

		for (String group : groups) {
			ModelRequest request = new ModelRequest(group, date.getTime());
			MetricReport report = m_service.invoke(request);

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
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				int minute = Calendar.getInstance().get(Calendar.MINUTE);
				String minuteStr = String.valueOf(minute);

				if (minute < 10) {
					minuteStr = '0' + minuteStr;
				}
				Transaction t = Cat.newTransaction("NetGraph", "M" + minuteStr);
				
				try {
					NetGraph netGraphTemplate = m_netGraphConfigManager.getConfig().getNetGraphs().get(0);
					Set<String> groups = new HashSet<String>();

					for (NetTopology netTopology : netGraphTemplate.getNetTopologies()) {
						for (Connection connection : netTopology.getConnections()) {
							for (Interface inter : connection.getInterfaces()) {
								groups.add(inter.getGroup());
							}
						}
					}
					
					Map<String, MetricReport> currentMetricReports = queryMetricReports(groups, TimeUtil.getCurrentHour());
					List<String> alertKeys = m_alertInfo.queryLastestAlarmKey(5);
					
					m_currentNetGraphSet = m_netGraphBuilder.buildGraphSet(netGraphTemplate, currentMetricReports, alertKeys);

					Date lastHour = new Date(TimeUtil.getCurrentHour().getTime() - TimeUtil.ONE_HOUR);
					Map<String, MetricReport> lastHourReports = queryMetricReports(groups, lastHour);

					m_lastNetGraphSet = m_netGraphBuilder.buildGraphSet(netGraphTemplate, lastHourReports, new ArrayList<String>());
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				} finally {
					t.complete();
				}

				try {
					long duration = System.currentTimeMillis() - current;

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
