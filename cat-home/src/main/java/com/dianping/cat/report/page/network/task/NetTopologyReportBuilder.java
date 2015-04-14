package com.dianping.cat.report.page.network.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.network.entity.Connection;
import com.dianping.cat.home.network.entity.Interface;
import com.dianping.cat.home.network.entity.NetGraph;
import com.dianping.cat.home.network.entity.NetGraphSet;
import com.dianping.cat.home.network.entity.NetTopology;
import com.dianping.cat.home.network.transform.DefaultNativeBuilder;
import com.dianping.cat.report.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.page.network.service.NetTopologyReportService;
import com.dianping.cat.report.task.TaskBuilder;

public class NetTopologyReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_NET_TOPOLOGY;

	@Inject
	protected NetTopologyReportService m_reportService;

	@Inject
	protected MetricReportService m_metricReportService;

	@Inject
	private NetGraphBuilder m_netGraphBuilder;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no daily report builder for net topology!");
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		NetGraph netGraphTemplate = m_netGraphConfigManager.getConfig().getNetGraphs().get(0);
		Set<String> groups = new HashSet<String>();

		for (NetTopology netTopology : netGraphTemplate.getNetTopologies()) {
			for (Connection connection : netTopology.getConnections()) {
				for (Interface inter : connection.getInterfaces()) {
					groups.add(inter.getGroup());
				}
			}
		}

		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();

		for (String group : groups) {
			Date end = new Date(period.getTime() + TimeHelper.ONE_HOUR);
			MetricReport report = m_metricReportService.queryReport(group, period, end);

			reports.put(group, report);
		}

		NetGraphSet netGraphSet = m_netGraphBuilder
		      .buildGraphSet(netGraphTemplate, reports, new ArrayList<AlertMetric>());
		HourlyReport hourlyReport = new HourlyReport();

		hourlyReport.setType(1);
		hourlyReport.setName(name);
		hourlyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		hourlyReport.setDomain(domain);
		hourlyReport.setPeriod(period);
		byte[] content = DefaultNativeBuilder.build(netGraphSet);
		return m_reportService.insertHourlyReport(hourlyReport, content);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no monthly report builder for net topology!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no weekly report builder for net topology!");
	}

}
