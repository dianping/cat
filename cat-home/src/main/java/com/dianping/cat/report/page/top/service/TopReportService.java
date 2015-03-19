package com.dianping.cat.report.page.top.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

public class TopReportService extends AbstractReportService<TopReport> {

	@Override
	public TopReport makeReport(String domain, Date start, Date end) {
		TopReport report = new TopReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public TopReport queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("Top report don't support daily report");
	}

	private TopReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TopReport(domain);
		}
	}

	@Override
	public TopReport queryHourlyReport(String domain, Date start, Date end) {
		TopReportMerger merger = new TopReportMerger(new TopReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = TopAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						TopReport reportModel = queryFromHourlyBinary(report.getId(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		TopReport topReport = merger.getTopReport();

		topReport.setStartTime(start);
		topReport.setEndTime(new Date(end.getTime() - 1));
		return topReport;
	}

	@Override
	public TopReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Top report don't support monthly report");
	}

	@Override
	public TopReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Top report don't support weekly report");
	}

}
