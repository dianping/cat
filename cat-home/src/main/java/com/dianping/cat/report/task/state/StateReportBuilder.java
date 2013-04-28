package com.dianping.cat.report.task.state;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.home.dal.report.Monthreport;
import com.dianping.cat.home.dal.report.Weeklyreport;
import com.dianping.cat.report.page.model.state.StateReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class StateReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	@Inject
	private StateMerger m_stateMerger;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getDailyReport(reportName, reportDomain, reportPeriod);
			m_dailyReportDao.insert(report);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("State report don't support HourReport!");
	}

	private StateReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				Dailyreport dailyreport = m_dailyReportDao.findByNameDomainPeriod(new Date(startTime), domain,
				      "state", DailyreportEntity.READSET_FULL);
				String xml = dailyreport.getContent();
				
				StateReport reportModel = DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();
		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	@Override
	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.MONTH, 1);

		Date start = reportPeriod;
		Date end = cal.getTime();

		StateReport stateReport = buildMergedDailyReport(reportDomain, start, end);
		Monthreport report = m_monthreportDao.createLocal();

		report.setContent(stateReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_monthreportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod) {
		Date start = reportPeriod;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		StateReport stateReport = buildMergedDailyReport(reportDomain, start, end);
		Weeklyreport report = m_weeklyreportDao.createLocal();
		String content = stateReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_weeklyreportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private Dailyreport getDailyReport(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = getDomainsFromHourlyReport(reportPeriod, endDate);
		
		List<Report> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		String content = m_stateMerger.mergeForDaily(reportDomain, reports, domainSet).toString();

		Dailyreport report = m_dailyReportDao.createLocal();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);
		return report;
	}

}
