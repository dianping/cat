package com.dianping.cat.report.task.network;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.network.nettopology.NetGraphManager;
import com.dianping.cat.report.service.impl.DefaultReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class NetTopologyHistoryReportBuilder implements ReportTaskBuilder {

	@Inject
	protected DefaultReportService m_defaultReportService;
	
	@Inject
	private NetGraphManager m_netGraphManager;

	@Override
   public boolean buildDailyTask(String name, String domain, Date period) {
	   return false;
   }

	@Override
   public boolean buildHourlyTask(String name, String domain, Date period) {
		NetGraphSet netGraphSet = m_netGraphManager.buildSet(period.getTime());
		HourlyReport hourlyReport = new HourlyReport();
		
		hourlyReport.setType(1);
		hourlyReport.setName(name);
		hourlyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		hourlyReport.setDomain(domain);
		hourlyReport.setPeriod(period);
		hourlyReport.setContent("");
		byte[] content = DefaultNativeBuilder.build(netGraphSet);
		return m_defaultReportService.insertHourlyReport(hourlyReport, content);
   }

	@Override
   public boolean buildMonthlyTask(String name, String domain, Date period) {
	   return false;
   }

	@Override
   public boolean buildWeeklyTask(String name, String domain, Date period) {
	   return false;
   }

}
