package com.dianping.cat.report.task;

import java.util.Date;

public interface TaskBuilder {

	public boolean buildDailyTask(String name, String domain, Date period);

	public boolean buildHourlyTask(String name, String domain, Date period);

	public boolean buildMonthlyTask(String name, String domain, Date period);

	public boolean buildWeeklyTask(String name, String domain, Date period);

}
