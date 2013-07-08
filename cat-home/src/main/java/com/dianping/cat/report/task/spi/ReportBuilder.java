package com.dianping.cat.report.task.spi;

import java.util.Date;

public interface ReportBuilder {

	public boolean buildDailyReport(String name, String domain, Date period);

	public boolean buildHourReport(String name, String domain, Date period);

	public boolean buildMonthReport(String name, String domain, Date period);

	public boolean buildWeeklyReport(String name, String domain, Date period);

}
