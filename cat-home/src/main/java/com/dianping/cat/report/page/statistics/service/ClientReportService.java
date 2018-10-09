package com.dianping.cat.report.page.statistics.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class ClientReportService extends AbstractReportService<ClientReport> {

	@Override
	public ClientReport makeReport(String domain, Date start, Date end) {
		ClientReport report = new ClientReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public ClientReport queryDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		String name = Constants.REPORT_CLIENT;

		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
			      DailyReportEntity.READSET_FULL);
			return queryFromDailyBinary(report.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		ClientReport report = new ClientReport(Constants.CAT);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	private ClientReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ClientReport(domain);
		}
	}

	@Override
	public ClientReport queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("Client report service do not suppot queryHourlyReport feature");
	}

	@Override
	public ClientReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Client report service do not suppot queryMonthlyReport feature");
	}

	@Override
	public ClientReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Client report service do not suppot queryWeeklyReport feature");
	}

}
