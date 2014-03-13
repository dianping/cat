package com.dianping.cat.report.task.spi;

import java.util.Date;

public interface ReportTaskBuilder {

	public boolean buildDailyTask(String name, String domain, Date period);

	public boolean buildHourlyTask(String name, String domain, Date period);

	public boolean buildMonthlyTask(String name, String domain, Date period);

	public boolean buildWeeklyTask(String name, String domain, Date period);

}
