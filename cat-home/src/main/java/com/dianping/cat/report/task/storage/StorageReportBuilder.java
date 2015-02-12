package com.dianping.cat.report.task.storage;

import java.util.Date;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.report.task.TaskBuilder;

public class StorageReportBuilder implements TaskBuilder {

	public static final String ID = StorageAnalyzer.ID;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		return true;
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		return true;
	}

}
