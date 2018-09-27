package com.dianping.cat.report.page.server.task;

import java.util.Calendar;
import java.util.Date;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.page.server.service.MetricGraphService;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = MetricGraphPruner.ID)
public class MetricGraphPruner implements TaskBuilder {

	public static final String ID = Constants.METRIC_GRAPH_PRUNER;

	@Inject
	private MetricGraphService m_graphService;

	private static final int DURATION = -2;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Threads.forGroup("cat").start(new DeleteTask());

		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support weekly task");
	}

	public Date queryPeriod(int months) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	public class DeleteTask implements Task {

		@Override
		public String getName() {
			return "delete-metric-graph-job";
		}

		@Override
		public void run() {
			try {
				Date period = queryPeriod(DURATION);

				m_graphService.deleteBeforeDate(period);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
