package com.dianping.cat.report.analyzer;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.task.TaskBuilder;

public class CapacityAnalyzer extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		TaskBuilder builder = lookup(TaskBuilder.class, CapacityUpdateTask.ID);

		builder.buildHourlyTask("cat", "cat", null);
		builder.buildDailyTask("cat", "cat", null);
		builder.buildWeeklyTask("cat", "cat", null);
		builder.buildMonthlyTask("cat", "cat", null);
	}

}
