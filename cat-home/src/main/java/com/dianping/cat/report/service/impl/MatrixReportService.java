package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportMerger;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;
import com.dianping.cat.report.service.AbstractReportService;

public class MatrixReportService extends AbstractReportService<MatrixReport> {

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

				if (xml != null && xml.length() > 0) {
					MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} else {
					MatrixReport reportModel = queryFromDailyBinary(report.getId(), domain);

					reportModel.accept(merger);
				}
			} catch (DalNotFoundException e) {
				m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start + " " + end);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

	private MatrixReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new MatrixReport(domain);
		}
	}

	private MatrixReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new MatrixReport(domain);
		}
	}

	private MatrixReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new MatrixReport(domain);
		}
	}

	private MatrixReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new MatrixReport(domain);
		}
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
						if (xml != null && xml.length() > 0) {// for old xml storage
							MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser
							      .parse(xml);
							reportModel.accept(merger);
						} else {// for new binary storage, binary is same to report id
							MatrixReport reportModel = queryFromHourlyBinary(report.getId(), domain);

							reportModel.accept(merger);
						}
					} catch (DalNotFoundException e) {
						m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start + " " + end);
					} catch (Exception e) {
						Cat.logError(e);
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

			if (content != null && content.length() > 0) {
				return com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromMonthlyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
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

			if (content != null && content.length() > 0) {
				return com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromWeeklyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new MatrixReport(domain);
	}

}
