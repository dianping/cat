package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Message;
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

	@Override
	public TopReport queryHourlyReport(String domain, Date start, Date end) {
		TopReportMerger merger = new TopReportMerger(new TopReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "top";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					String xml = report.getContent();

					try {
						TopReport reportModel = com.dianping.cat.consumer.top.model.transform.DefaultSaxParser.parse(xml);
						reportModel.accept(merger);
					} catch (Exception e) {
						Cat.logError(e);
						Cat.getProducer().logEvent("ErrorXML", name, Message.SUCCESS,
						      report.getDomain() + " " + report.getPeriod() + " " + report.getId());
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
