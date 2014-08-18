package com.dianping.cat.report.task.network;

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
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.nettopo.entity.Connection;
import com.dianping.cat.home.nettopo.entity.Interface;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.home.nettopo.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.system.config.NetGraphConfigManager;

public class NetTopologyReportBuilder implements ReportTaskBuilder {

	public static final String ID = Constants.REPORT_NET_TOPOLOGY;

	@Inject
	protected ReportServiceManager m_reportService;

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
			Date end = new Date(period.getTime() + TimeUtil.ONE_HOUR);
			MetricReport report = m_reportService.queryMetricReport(group, period, end);

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
		hourlyReport.setContent("");
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
