package com.dianping.cat.report.page.dependency.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
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
		throw new UnsupportedOperationException("Dependency report don't support daily report");
	}

	private DependencyReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new DependencyReport(domain);
		}
	}

	@Override
	public DependencyReport queryHourlyReport(String domain, Date start, Date end) {
		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = DependencyAnalyzer.ID;

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
						DependencyReport reportModel = queryFromHourlyBinary(report.getId(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
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
		throw new UnsupportedOperationException("Dependency report don't support monthly report");
	}

	@Override
	public DependencyReport queryWeeklyReport(String domain, Date start) {
		throw new UnsupportedOperationException("Dependency report don't support weekly report");
	}

}
