package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.report.service.AbstractReportService;

public class NetTopologyReportService extends AbstractReportService<NetGraphSet> {

	@Override
	public NetGraphSet makeReport(String domain, Date start, Date end) {
		NetGraphSet report = new NetGraphSet();

		return report;
	}

	@Override
	public NetGraphSet queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("net topology report don't support daily report");
	}

	@Override
	public NetGraphSet queryHourlyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		String name = Constants.REPORT_NET_TOPOLOGY;
		NetGraphSet netGraphSet = null;
		List<HourlyReport> reports = null;

		try {
			reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
			      HourlyReportEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}

		if (reports != null && reports.size() > 0) {
			String xml = reports.get(0).getContent();

			if (xml != null && xml.length() > 0) {
				try {
					netGraphSet = com.dianping.cat.home.nettopo.transform.DefaultSaxParser.parse(xml);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		return netGraphSet;
	}

	@Override
	public NetGraphSet queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("net topology report don't support monthly report");
	}

	@Override
	public NetGraphSet queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("net topology report don't support weekly report");
	}

}
