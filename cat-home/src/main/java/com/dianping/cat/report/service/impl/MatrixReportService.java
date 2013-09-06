package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportMerger;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Message;
import com.dianping.cat.report.service.AbstractReportService;

public class MatrixReportService extends AbstractReportService<MatrixReport> {

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Override
	public MatrixReport makeReport(String domain, Date start, Date end) {
		MatrixReport report = new MatrixReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public MatrixReport queryDailyReport(String domain, Date start, Date end) {
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = MatrixAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

	@Override
	public MatrixReport queryHourlyReport(String domain, Date start, Date end) {
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = MatrixAnalyzer.ID;

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
						MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser
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
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(new Date(end.getTime() - 1));

		Set<String> domains = queryAllDomainNames(start, end, MatrixAnalyzer.ID);
		matrixReport.getDomainNames().addAll(domains);
		return matrixReport;
	}

	@Override
	public MatrixReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, MatrixAnalyzer.ID,
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new MatrixReport(domain);
	}

	@Override
	public MatrixReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, MatrixAnalyzer.ID,
			      WeeklyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new MatrixReport(domain);
	}

}
