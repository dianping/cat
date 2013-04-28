package com.dianping.cat.report.task.spi;

import java.util.Date;

public interface ReportBuilder {

	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod);

	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod);

	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod);

	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod);

}
