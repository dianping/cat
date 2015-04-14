package com.dianping.cat.report.task.storage;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.storage.task.StorageReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class StorageReportBuilderTest extends ComponentTestCase {

	// @Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, StorageReportBuilder.ID);

		builder.buildDailyTask(StorageReportBuilder.ID, "cat-SQL", TimeHelper.getYesterday());
		builder.buildWeeklyTask(StorageReportBuilder.ID, "cat-SQL", TimeHelper.getCurrentWeek());
		builder.buildMonthlyTask(StorageReportBuilder.ID, "cat-SQL", TimeHelper.getLastMonth());
	}

}
