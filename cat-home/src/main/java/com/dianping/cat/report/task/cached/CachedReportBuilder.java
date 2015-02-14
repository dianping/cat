package com.dianping.cat.report.task.cached;

import java.util.Date;

import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;

public class CachedReportBuilder implements TaskBuilder {

	public static final String ID = Constants.CACHED_REPORT;

	@Inject
	private CachedReportTask m_cachedReportTask;
	
	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Threads.forGroup(Constants.CAT).start(m_cachedReportTask);
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support weekly task");
	}

}
