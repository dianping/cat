package com.dianping.cat.report.task.highload;

import java.util.Date;

import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public abstract class HighLoadUpdater implements ReportTaskBuilder {

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support daily update");
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support hourly update");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support monthly update");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support weekly update");
	}

	public abstract String getID();

}
