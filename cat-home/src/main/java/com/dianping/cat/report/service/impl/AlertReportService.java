package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
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
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.alertReport.transform.DefaultNativeParser;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.report.task.exceptionAlert.AlertReportMerger;

public class AlertReportService extends AbstractReportService<AlertReport> {

	@Override
	public AlertReport makeReport(String domain, Date start, Date end) {
		AlertReport report = new AlertReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public AlertReport queryDailyReport(String domain, Date start, Date end) {
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_ALERT;

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();

				if (xml != null && xml.length() > 0) {
					AlertReport reportModel = com.dianping.cat.home.alertReport.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} else {
					AlertReport reportModel = queryFromDailyBinary(report.getId(), domain);
					reportModel.accept(merger);
				}
			} catch (DalNotFoundException e) {
				m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start + " " + end);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		AlertReport alertReport = merger.getAlertReport();

		alertReport.setStartTime(start);
		alertReport.setEndTime(end);
		return alertReport;
	}

	@Override
	public AlertReport queryHourlyReport(String domain, Date start, Date end) {
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_ALERT;

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
						if (xml != null && xml.length() > 0) {
							AlertReport reportModel = com.dianping.cat.home.alertReport.transform.DefaultSaxParser.parse(xml);
							reportModel.accept(merger);
						} else {
							AlertReport reportModel = queryFromHourlyBinary(report.getId(), domain);
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
		AlertReport alertReport = merger.getAlertReport();

		alertReport.setStartTime(start);
		alertReport.setEndTime(end);

		return alertReport;
	}

	@Override
	public AlertReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, Constants.REPORT_ALERT,
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			if (content != null && content.length() > 0) {
				return com.dianping.cat.home.alertReport.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromMonthlyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AlertReport(domain);
	}

	@Override
	public AlertReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, Constants.REPORT_ALERT,
			      WeeklyReportEntity.READSET_FULL);
			String content = entity.getContent();

			if (content != null && content.length() > 0) {
				return com.dianping.cat.home.alertReport.transform.DefaultSaxParser.parse(content);
			} else {
				return queryFromWeeklyBinary(entity.getId(), domain);
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + domain + " " + start);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AlertReport(domain);
	}

	private AlertReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AlertReport(domain);
		}
	}

	private AlertReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AlertReport(domain);
		}
	}

	private AlertReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AlertReport(domain);
		}
	}

	private AlertReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AlertReport(domain);
		}
	}

}
