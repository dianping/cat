package com.dianping.cat.report.task.network;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class NetTopologyReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Inject
	private NetGraphBuilder m_netGraphBuilder;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no daily report builder for net topology!");
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		Set<String> groups = m_netGraphBuilder.buildRequireGroups();
		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();

		for (String group : groups) {
			Date end = new Date(period.getTime() + TimeUtil.ONE_HOUR);
			MetricReport report = m_reportService.queryMetricReport(group, period, end);

			reports.put(group, report);
		}
		NetGraphSet netGraphSet = m_netGraphBuilder.buildSet(reports);
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
