package com.dianping.cat.report.task.service;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.statistics.task.service.ClientReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class ClientReportBuilderTest extends ComponentTestCase {

	@Test
	public void test() {
		TaskBuilder builder = lookup(TaskBuilder.class, ClientReportBuilder.ID);

		builder.buildDailyTask(ClientReportBuilder.ID, Constants.CAT, TimeHelper.getYesterday());
	}
}
