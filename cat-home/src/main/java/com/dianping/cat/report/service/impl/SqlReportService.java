package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.SqlReportMerger;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultNativeParser;
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

public class SqlReportService extends AbstractReportService<SqlReport> {

	@Override
	public SqlReport makeReport(String domain, Date start, Date end) {
		SqlReport report = new SqlReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public SqlReport queryDailyReport(String domain, Date start, Date end) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = SqlAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();

				if (xml != null && xml.length() > 0) {
					SqlReport reportModel = com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} else {
					SqlReport reportModel = queryFromDailyBinary(report.getId(), domain);

					reportModel.accept(merger);
				}
			} catch (DalNotFoundException e) {
				m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start + " " + end);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		SqlReport sqlReport = merger.getSqlReport();

		sqlReport.setStartTime(start);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

	private SqlReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new SqlReport(domain);
		}
	}

	private SqlReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new SqlReport(domain);
		}
	}

	private SqlReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new SqlReport(domain);
		}
	}

	private SqlReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new SqlReport(domain);
		}
	}

	@Override
	public SqlReport queryHourlyReport(String domain, Date start, Date end) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = SqlAnalyzer.ID;

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
							SqlReport reportModel = com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(xml);
							reportModel.accept(merger);
						} else {// for new binary storage, binary is same to report id
							SqlReport reportModel = queryFromHourlyBinary(report.getId(), domain);

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
		SqlReport sqlReport = merger.getSqlReport();

		sqlReport.setStartTime(start);
		sqlReport.setEndTime(new Date(end.getTime() - 1));

		Set<String> domains = queryAllDomainNames(start, end, SqlAnalyzer.ID);
		sqlReport.getDomainNames().addAll(domains);
		return sqlReport;
	}

	@Override
	public SqlReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, SqlAnalyzer.ID,
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			if (content != null && content.length() > 0) {
				return com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromMonthlyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new SqlReport(domain);
	}

	@Override
	public SqlReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, SqlAnalyzer.ID,
			      WeeklyReportEntity.READSET_FULL);
			String content = entity.getContent();

			if (content != null && content.length() > 0) {
				return com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromWeeklyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new SqlReport(domain);
	}

}
