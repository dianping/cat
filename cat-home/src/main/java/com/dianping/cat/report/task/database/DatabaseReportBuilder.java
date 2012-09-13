package com.dianping.cat.report.task.database;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class DatabaseReportBuilder extends AbstractReportBuilder implements ReportBuilder {
	
	@Inject
	private DatabaseMerger m_databaseMerger;

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

	private Dailyreport getDailyReport(String reportName, String reportDatabase, Date reportPeriod) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> databaseSet = new HashSet<String>();
		getDatabaseSet(databaseSet, reportPeriod, endDate);
		List<Report> reports = m_reportDao.findDatabaseAllByDomainNameDuration(reportPeriod, endDate, reportDatabase, reportName,
		      ReportEntity.READSET_FULL);
		String content = m_databaseMerger.mergeForDaily(reportDatabase, reports, databaseSet).toString();

		Dailyreport report = m_dailyReportDao.createLocal();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDatabase);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(2);
		return report;
	}
	
	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod){
		throw new RuntimeException("Database report don't support HourReport!");
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getDailyReport(reportName, reportDomain, reportPeriod);
			clearDailyReport(report);
			m_dailyReportDao.insert(report);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}
	
	@Override
	protected void clearDailyReport(Dailyreport report) throws DalException {
		this.m_dailyReportDao.deleteDatabaseByDomainNamePeriod(report);
	}
	
	@Override
	public boolean redoHourReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Database report don't support redo HourReport!");
	}

}
