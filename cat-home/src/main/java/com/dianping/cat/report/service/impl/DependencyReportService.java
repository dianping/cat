package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Message;
import com.dianping.cat.report.service.AbstractReportService;

public class DependencyReportService extends AbstractReportService<DependencyReport> {

	@Override
	public DependencyReport makeReport(String domain, Date start, Date end) {
		DependencyReport report = new DependencyReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public DependencyReport queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("Dependency report don't support daily report");
	}

	@Override
	public DependencyReport queryHourlyReport(String domain, Date start, Date end) {
		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = DependencyAnalyzer.ID;

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
						DependencyReport reportModel = com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser
						      .parse(xml);
						reportModel.accept(merger);
					} catch (Exception e) {
						Cat.logError(e);
						Cat.getProducer().logEvent("ErrorXML", name, Message.SUCCESS,
						      report.getDomain() + " " + report.getPeriod() + " " + report.getId());
					}
				}
			}
		}
		DependencyReport dependencyReport = merger.getDependencyReport();

		dependencyReport.setStartTime(start);
		dependencyReport.setEndTime(new Date(end.getTime() - 1));

		Set<String> domains = queryAllDomainNames(start, end, DependencyAnalyzer.ID);
		dependencyReport.getDomainNames().addAll(domains);
		return dependencyReport;
	}

	@Override
	public DependencyReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Dependency report don't support monthly report");
	}

	@Override
	public DependencyReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Dependency report don't support weekly report");
	}

}
